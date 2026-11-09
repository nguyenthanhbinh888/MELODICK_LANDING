package com.melodick.recs.service.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TikTokPublicClient {

    private static final Logger log = LoggerFactory.getLogger(TikTokPublicClient.class);
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

    private static final Pattern HANDLE_PATTERN = Pattern.compile("tiktok\\.com\\/@([a-zA-Z0-9_.-]+)");
    private static final Pattern FOLLOWER_COUNT_PATTERN = Pattern.compile("\"followerCount\"\\s*:\\s*(\\d+)");
    private static final Pattern HEART_COUNT_PATTERN = Pattern.compile("\"heartCount\"\\s*:\\s*(\\d+)|\"heart\"\\s*:\\s*(\\d+)");
    private static final Pattern NICKNAME_PATTERN = Pattern.compile("\"nickname\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern HTML_FOLLOWER_TAG_PATTERN = Pattern.compile("data-e2e=[\"']followers-count[\"']>([^<]+)<");

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public TikTokPublicClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(4));
        factory.setReadTimeout(Duration.ofSeconds(4));

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
                .build();
    }

    public static boolean isTikTokUrl(String url) {
        if (!StringUtils.hasText(url)) return false;
        String lower = url.trim().toLowerCase();
        return lower.contains("tiktok.com");
    }

    public static String extractHandle(String url) {
        if (!StringUtils.hasText(url)) return null;
        Matcher matcher = HANDLE_PATTERN.matcher(url);
        if (matcher.find()) {
            return "@" + matcher.group(1);
        }
        return null;
    }

    public PlatformFetchResult fetchStats(String linkUrl) {
        if (!StringUtils.hasText(linkUrl)) {
            return PlatformFetchResult.failed("TikTok", "TIKTOK_OEMBED");
        }

        String cleanUrl = linkUrl.trim();
        String handle = extractHandle(cleanUrl);

        // 1. Try public profile HTML scraping
        PlatformFetchResult htmlResult = fetchFromProfileHtml(cleanUrl, handle);
        if (htmlResult.isSuccess() && htmlResult.getFollowersCount() > 0) {
            return htmlResult;
        }

        // 2. Try TikTok oEmbed (Official Public Endpoint)
        PlatformFetchResult oembedResult = fetchFromOembed(cleanUrl, handle);
        if (oembedResult.isSuccess()) {
            if (htmlResult.isSuccess() && htmlResult.getFollowersCount() > 0) {
                oembedResult.setFollowersCount(htmlResult.getFollowersCount());
                oembedResult.setTiktokFollowers(htmlResult.getFollowersCount());
            }
            return oembedResult;
        }

        return htmlResult.isSuccess() ? htmlResult : PlatformFetchResult.failed("TikTok", "TIKTOK_OEMBED");
    }

    private PlatformFetchResult fetchFromProfileHtml(String cleanUrl, String handle) {
        try {
            String html = restClient.get()
                    .uri(cleanUrl)
                    .header(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .retrieve()
                    .body(String.class);

            if (StringUtils.hasText(html)) {
                long followers = 0L;
                long likes = 0L;
                String nickname = handle;

                Matcher followerMatcher = FOLLOWER_COUNT_PATTERN.matcher(html);
                if (followerMatcher.find()) {
                    followers = Long.parseLong(followerMatcher.group(1));
                } else {
                    Matcher tagMatcher = HTML_FOLLOWER_TAG_PATTERN.matcher(html);
                    if (tagMatcher.find()) {
                        followers = parseCompactMetric(tagMatcher.group(1));
                    }
                }

                Matcher heartMatcher = HEART_COUNT_PATTERN.matcher(html);
                if (heartMatcher.find()) {
                    String g = heartMatcher.group(1) != null ? heartMatcher.group(1) : heartMatcher.group(2);
                    likes = Long.parseLong(g);
                }

                Matcher nickMatcher = NICKNAME_PATTERN.matcher(html);
                if (nickMatcher.find()) {
                    nickname = nickMatcher.group(1);
                }

                if (followers > 0 || StringUtils.hasText(nickname)) {
                    PlatformFetchResult result = new PlatformFetchResult();
                    result.setPlatform("TikTok");
                    result.setSource("TIKTOK_PUBLIC");
                    result.setSuccess(true);
                    result.setChannelTitle(StringUtils.hasText(nickname) ? nickname : handle);
                    result.setFollowersCount(followers);
                    result.setTiktokFollowers(followers);
                    result.setTiktokLikes(likes);
                    result.setTiktokHandle(handle);
                    result.setTopVideoUrl(cleanUrl);
                    return result;
                }
            }
        } catch (Exception e) {
            log.debug("TikTok profile scraping failed for [{}]: {}", cleanUrl, e.getMessage());
        }

        return PlatformFetchResult.failed("TikTok", "TIKTOK_PUBLIC");
    }

    private PlatformFetchResult fetchFromOembed(String cleanUrl, String handle) {
        try {
            String oembedUrl = "https://www.tiktok.com/oembed?url=" + cleanUrl;
            String response = restClient.get()
                    .uri(oembedUrl)
                    .retrieve()
                    .body(String.class);

            if (StringUtils.hasText(response)) {
                JsonNode node = objectMapper.readTree(response);
                PlatformFetchResult result = new PlatformFetchResult();
                result.setPlatform("TikTok");
                result.setSource("TIKTOK_OEMBED");
                result.setSuccess(true);
                String authorName = node.path("author_name").asText(handle != null ? handle : "TikTok Creator");
                result.setChannelTitle(authorName);
                result.setTiktokHandle(handle);
                result.setTopVideoUrl(cleanUrl);
                result.setTopVideoTitle(node.path("title").asText(authorName));
                result.setTopVideoThumbnail(node.path("thumbnail_url").asText());
                result.setFollowersCount(0L);
                result.setTiktokFollowers(0L);
                return result;
            }
        } catch (Exception e) {
            log.debug("TikTok oEmbed failed for [{}]: {}", cleanUrl, e.getMessage());
        }

        return PlatformFetchResult.failed("TikTok", "TIKTOK_OEMBED");
    }

    /**
     * Parses compact metric strings from TikTok HTML tags:
     * e.g. "12.5K" -> 12500, "1.2M" -> 1200000, "450" -> 450
     */
    public static long parseCompactMetric(String text) {
        if (!StringUtils.hasText(text)) return 0L;
        String clean = text.trim().toUpperCase().replace(",", "");
        try {
            if (clean.endsWith("B")) {
                double num = Double.parseDouble(clean.substring(0, clean.length() - 1));
                return (long) (num * 1_000_000_000L);
            }
            if (clean.endsWith("M")) {
                double num = Double.parseDouble(clean.substring(0, clean.length() - 1));
                return (long) (num * 1_000_000L);
            }
            if (clean.endsWith("K")) {
                double num = Double.parseDouble(clean.substring(0, clean.length() - 1));
                return (long) (num * 1_000L);
            }
            return Long.parseLong(clean);
        } catch (Exception ignored) {
            return 0L;
        }
    }
}
