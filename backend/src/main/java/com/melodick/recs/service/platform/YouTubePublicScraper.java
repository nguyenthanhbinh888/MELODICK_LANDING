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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class YouTubePublicScraper {

    private static final Logger log = LoggerFactory.getLogger(YouTubePublicScraper.class);
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";
    private static final Pattern CHANNEL_ID_HTML_PATTERN = Pattern.compile(
            "(?:\"browseId\"\\s*:\\s*\"|\"externalId\"\\s*:\\s*\"|canonical\"\\s*href=[\"']https://www\\.youtube\\.com/channel/|youtube\\.com/channel/|(?:channelId[\"']?\\s*:\\s*[\"']|<meta itemprop=[\"']channelId[\"'] content=[\"']))(UC[a-zA-Z0-9_-]{22})[\"'/]?"
    );
    private static final Pattern VIDEO_VIEWS_HTML_PATTERN = Pattern.compile("\"viewCount\"\\s*:\\s*\"(\\d+)\"");

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public YouTubePublicScraper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(4));
        factory.setReadTimeout(Duration.ofSeconds(4));

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
                .build();
    }

    public PlatformFetchResult fetchStats(String linkUrl) {
        if (!StringUtils.hasText(linkUrl)) {
            return PlatformFetchResult.failed("YouTube", "YOUTUBE_RSS");
        }

        try {
            String videoId = YouTubeUrlHelper.extractVideoId(linkUrl);
            String channelId = YouTubeUrlHelper.extractChannelId(linkUrl);
            String handle = YouTubeUrlHelper.extractHandle(linkUrl);

            if (StringUtils.hasText(channelId)) {
                PlatformFetchResult rssResult = fetchByChannelRss(channelId);
                if (rssResult.isSuccess()) {
                    return rssResult;
                }
            }

            if (StringUtils.hasText(handle)) {
                String resolvedChannelId = resolveHandleToChannelId(handle);
                if (StringUtils.hasText(resolvedChannelId)) {
                    PlatformFetchResult rssResult = fetchByChannelRss(resolvedChannelId);
                    if (rssResult.isSuccess()) {
                        return rssResult;
                    }
                }
            }

            if (StringUtils.hasText(videoId)) {
                PlatformFetchResult oembedResult = fetchByVideoOembed(videoId);
                if (oembedResult.isSuccess()) {
                    return oembedResult;
                }
            }
        } catch (Exception e) {
            log.warn("YouTube public scraping failed for link [{}]: {}", linkUrl, e.getMessage());
        }

        return PlatformFetchResult.failed("YouTube", "YOUTUBE_RSS");
    }

    public PlatformFetchResult fetchByChannelRss(String channelId) {
        try {
            String rssUrl = "https://www.youtube.com/feeds/videos.xml?channel_id=" + channelId;
            String xmlContent = restClient.get()
                    .uri(rssUrl)
                    .retrieve()
                    .body(String.class);

            if (!StringUtils.hasText(xmlContent)) {
                return PlatformFetchResult.failed("YouTube", "YOUTUBE_RSS");
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            dbFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new StringReader(xmlContent)));

            NodeList titleNodes = doc.getElementsByTagName("title");
            String channelTitle = titleNodes.getLength() > 0 ? titleNodes.item(0).getTextContent() : "YouTube Channel";

            NodeList entries = doc.getElementsByTagName("entry");
            if (entries.getLength() == 0) {
                return PlatformFetchResult.failed("YouTube", "YOUTUBE_RSS");
            }

            long highestViews = -1L;
            long totalViewsSum = 0L;
            String topVideoId = null;
            String topVideoTitle = null;
            String topVideoThumb = null;

            for (int i = 0; i < entries.getLength(); i++) {
                Element entry = (Element) entries.item(i);
                NodeList vidList = entry.getElementsByTagNameNS("http://www.youtube.com/xml/schemas/2015", "videoId");
                String vId = vidList.getLength() > 0 ? vidList.item(0).getTextContent() : null;

                NodeList tList = entry.getElementsByTagName("title");
                String title = tList.getLength() > 0 ? tList.item(0).getTextContent() : null;

                long views = 0L;
                NodeList statsList = entry.getElementsByTagNameNS("http://search.yahoo.com/mrss/", "statistics");
                if (statsList.getLength() > 0) {
                    Element statsEl = (Element) statsList.item(0);
                    String viewsStr = statsEl.getAttribute("views");
                    try {
                        views = Long.parseLong(viewsStr);
                    } catch (NumberFormatException ignored) {}
                }

                String thumb = null;
                NodeList thumbList = entry.getElementsByTagNameNS("http://search.yahoo.com/mrss/", "thumbnail");
                if (thumbList.getLength() > 0) {
                    thumb = ((Element) thumbList.item(0)).getAttribute("url");
                }
                if (!StringUtils.hasText(thumb) && StringUtils.hasText(vId)) {
                    thumb = YouTubeUrlHelper.buildThumbnailUrl(vId);
                }

                totalViewsSum += views;

                if (views > highestViews && StringUtils.hasText(vId)) {
                    highestViews = views;
                    topVideoId = vId;
                    topVideoTitle = title;
                    topVideoThumb = thumb;
                }
            }

            if (StringUtils.hasText(topVideoId)) {
                PlatformFetchResult result = new PlatformFetchResult();
                result.setPlatform("YouTube");
                result.setSource("YOUTUBE_RSS");
                result.setSuccess(true);
                result.setChannelTitle(channelTitle);
                result.setTotalViews(totalViewsSum > 0 ? totalViewsSum : (highestViews > 0 ? highestViews : 0L));
                result.setVideoCount(entries.getLength());
                result.setTopVideoId(topVideoId);
                result.setTopVideoTitle(topVideoTitle);
                result.setTopVideoUrl(YouTubeUrlHelper.buildWatchUrl(topVideoId));
                result.setTopVideoEmbedUrl(YouTubeUrlHelper.buildEmbedUrl(topVideoId));
                result.setTopVideoThumbnail(topVideoThumb);
                result.setTopVideoViews(highestViews > 0 ? highestViews : 0L);

                // Enrich with exact view count from watch page if RSS did not supply views
                if (result.getTopVideoViews() <= 0) {
                    enrichVideoViewsFromWatchPage(result, topVideoId);
                }

                return result;
            }
        } catch (Exception e) {
            log.debug("RSS fetch failed for channel [{}]: {}", channelId, e.getMessage());
        }
        return PlatformFetchResult.failed("YouTube", "YOUTUBE_RSS");
    }

    public PlatformFetchResult fetchByVideoOembed(String videoId) {
        try {
            String watchUrl = YouTubeUrlHelper.buildWatchUrl(videoId);
            String oembedUrl = "https://www.youtube.com/oembed?url=" + watchUrl + "&format=json";

            String response = restClient.get()
                    .uri(oembedUrl)
                    .retrieve()
                    .body(String.class);

            if (StringUtils.hasText(response)) {
                JsonNode node = objectMapper.readTree(response);
                PlatformFetchResult result = new PlatformFetchResult();
                result.setPlatform("YouTube");
                result.setSource("OEMBED");
                result.setSuccess(true);
                result.setChannelTitle(node.path("author_name").asText("YouTube Artist"));
                result.setTopVideoId(videoId);
                result.setTopVideoTitle(node.path("title").asText("Featured Release"));
                result.setTopVideoUrl(watchUrl);
                result.setTopVideoEmbedUrl(YouTubeUrlHelper.buildEmbedUrl(videoId));
                result.setTopVideoThumbnail(node.path("thumbnail_url").asText(YouTubeUrlHelper.buildThumbnailUrl(videoId)));

                // Enrich with exact view count from watch page HTML
                enrichVideoViewsFromWatchPage(result, videoId);

                return result;
            }
        } catch (Exception e) {
            log.debug("YouTube oEmbed failed for video [{}]: {}", videoId, e.getMessage());
        }
        return PlatformFetchResult.failed("YouTube", "OEMBED");
    }

    private void enrichVideoViewsFromWatchPage(PlatformFetchResult result, String videoId) {
        try {
            String watchUrl = YouTubeUrlHelper.buildWatchUrl(videoId);
            String html = restClient.get()
                    .uri(watchUrl)
                    .retrieve()
                    .body(String.class);

            if (StringUtils.hasText(html)) {
                Matcher matcher = VIDEO_VIEWS_HTML_PATTERN.matcher(html);
                if (matcher.find()) {
                    long views = Long.parseLong(matcher.group(1));
                    if (views > 0) {
                        result.setTopVideoViews(views);
                        result.setTotalViews(views);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Watch page view count scraping failed for video [{}]: {}", videoId, e.getMessage());
        }
    }

    private String resolveHandleToChannelId(String handle) {
        try {
            String cleanHandle = handle.startsWith("@") ? handle : "@" + handle;
            String channelPageUrl = "https://www.youtube.com/" + cleanHandle;

            String html = restClient.get()
                    .uri(channelPageUrl)
                    .retrieve()
                    .body(String.class);

            if (StringUtils.hasText(html)) {
                Matcher matcher = CHANNEL_ID_HTML_PATTERN.matcher(html);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }

            // Fallback for custom / vanity URLs without @
            if (!handle.startsWith("@")) {
                String altUrl = "https://www.youtube.com/c/" + handle;
                String altHtml = restClient.get()
                        .uri(altUrl)
                        .retrieve()
                        .body(String.class);
                if (StringUtils.hasText(altHtml)) {
                    Matcher altMatcher = CHANNEL_ID_HTML_PATTERN.matcher(altHtml);
                    if (altMatcher.find()) {
                        return altMatcher.group(1);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Failed to resolve handle [{}]: {}", handle, e.getMessage());
        }
        return null;
    }
}
