package com.melodick.recs.service.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Component
public class YouTubeApiClient {

    private static final Logger log = LoggerFactory.getLogger(YouTubeApiClient.class);
    private static final String YOUTUBE_API_BASE = "https://www.googleapis.com/youtube/v3";

    private final String apiKey;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public YouTubeApiClient(@Value("${app.youtube.api-key:}") String apiKey,
                            ObjectMapper objectMapper) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.objectMapper = objectMapper;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(4));
        factory.setReadTimeout(Duration.ofSeconds(4));

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .baseUrl(YOUTUBE_API_BASE)
                .build();
    }

    public boolean isConfigured() {
        return StringUtils.hasText(apiKey);
    }

    public PlatformFetchResult fetchStats(String linkUrl) {
        if (!isConfigured() || !StringUtils.hasText(linkUrl)) {
            return PlatformFetchResult.failed("YouTube", "YOUTUBE_API");
        }

        try {
            String videoId = YouTubeUrlHelper.extractVideoId(linkUrl);
            String channelId = YouTubeUrlHelper.extractChannelId(linkUrl);
            String handle = YouTubeUrlHelper.extractHandle(linkUrl);

            if (StringUtils.hasText(channelId)) {
                return fetchByChannelId(channelId);
            } else if (StringUtils.hasText(handle)) {
                return fetchByHandle(handle);
            } else if (StringUtils.hasText(videoId)) {
                return fetchByVideoId(videoId);
            }
        } catch (Exception e) {
            log.warn("YouTube Data API call failed for link [{}]: {}", linkUrl, e.getMessage());
        }

        return PlatformFetchResult.failed("YouTube", "YOUTUBE_API");
    }

    private PlatformFetchResult fetchByChannelId(String channelId) {
        try {
            String uri = String.format("/channels?part=snippet,statistics&id=%s&key=%s", channelId, apiKey);
            String responseBody = restClient.get().uri(uri).retrieve().body(String.class);
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode items = root.path("items");
            if (items.isArray() && !items.isEmpty()) {
                JsonNode channelItem = items.get(0);
                return buildResultFromChannelNode(channelItem, channelId);
            }
        } catch (Exception e) {
            log.warn("Failed to fetch channel ID [{}]: {}", channelId, e.getMessage());
        }
        return PlatformFetchResult.failed("YouTube", "YOUTUBE_API");
    }

    private PlatformFetchResult fetchByHandle(String handle) {
        try {
            String cleanHandle = handle.startsWith("@") ? handle : "@" + handle;
            String uri = String.format("/channels?part=snippet,statistics&forHandle=%s&key=%s", cleanHandle, apiKey);
            String responseBody = restClient.get().uri(uri).retrieve().body(String.class);
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode items = root.path("items");
            if (items.isArray() && !items.isEmpty()) {
                JsonNode channelItem = items.get(0);
                String channelId = channelItem.path("id").asText();
                return buildResultFromChannelNode(channelItem, channelId);
            }
        } catch (Exception e) {
            log.warn("Failed to fetch handle [{}]: {}", handle, e.getMessage());
        }
        return PlatformFetchResult.failed("YouTube", "YOUTUBE_API");
    }

    private PlatformFetchResult fetchByVideoId(String videoId) {
        try {
            String uri = String.format("/videos?part=snippet,statistics&id=%s&key=%s", videoId, apiKey);
            String responseBody = restClient.get().uri(uri).retrieve().body(String.class);
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode items = root.path("items");
            if (items.isArray() && !items.isEmpty()) {
                JsonNode videoItem = items.get(0);
                JsonNode snippet = videoItem.path("snippet");
                JsonNode statistics = videoItem.path("statistics");

                PlatformFetchResult result = new PlatformFetchResult();
                result.setPlatform("YouTube");
                result.setSource("YOUTUBE_API");
                result.setSuccess(true);
                result.setChannelTitle(snippet.path("channelTitle").asText("YouTube Channel"));

                long videoViews = statistics.path("viewCount").asLong(0L);
                result.setTotalViews(videoViews);
                result.setTopVideoId(videoId);
                result.setTopVideoTitle(snippet.path("title").asText("Featured Track"));
                result.setTopVideoUrl(YouTubeUrlHelper.buildWatchUrl(videoId));
                result.setTopVideoEmbedUrl(YouTubeUrlHelper.buildEmbedUrl(videoId));
                result.setTopVideoViews(videoViews);

                String thumb = snippet.path("thumbnails").path("high").path("url").asText();
                if (!StringUtils.hasText(thumb)) {
                    thumb = YouTubeUrlHelper.buildThumbnailUrl(videoId);
                }
                result.setTopVideoThumbnail(thumb);

                // Attempt to enrich with channel stats
                String channelId = snippet.path("channelId").asText();
                if (StringUtils.hasText(channelId)) {
                    enrichWithChannelStats(result, channelId);
                }

                return result;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch video ID [{}]: {}", videoId, e.getMessage());
        }
        return PlatformFetchResult.failed("YouTube", "YOUTUBE_API");
    }

    private PlatformFetchResult buildResultFromChannelNode(JsonNode channelItem, String channelId) {
        PlatformFetchResult result = new PlatformFetchResult();
        result.setPlatform("YouTube");
        result.setSource("YOUTUBE_API");
        result.setSuccess(true);

        JsonNode snippet = channelItem.path("snippet");
        JsonNode statistics = channelItem.path("statistics");

        result.setChannelTitle(snippet.path("title").asText());
        result.setTotalViews(statistics.path("viewCount").asLong(0L));
        result.setFollowersCount(statistics.path("subscriberCount").asLong(0L));
        result.setVideoCount(statistics.path("videoCount").asInt(0));

        // Fetch hottest video for this channel
        fetchTopVideoForChannel(result, channelId);

        return result;
    }

    private void fetchTopVideoForChannel(PlatformFetchResult result, String channelId) {
        try {
            String uri = String.format("/search?part=snippet&channelId=%s&order=viewCount&type=video&maxResults=1&key=%s", channelId, apiKey);
            String responseBody = restClient.get().uri(uri).retrieve().body(String.class);
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode items = root.path("items");
            if (items.isArray() && !items.isEmpty()) {
                JsonNode searchItem = items.get(0);
                String videoId = searchItem.path("id").path("videoId").asText();
                if (StringUtils.hasText(videoId)) {
                    result.setTopVideoId(videoId);
                    result.setTopVideoTitle(searchItem.path("snippet").path("title").asText());
                    result.setTopVideoUrl(YouTubeUrlHelper.buildWatchUrl(videoId));
                    result.setTopVideoEmbedUrl(YouTubeUrlHelper.buildEmbedUrl(videoId));
                    result.setTopVideoThumbnail(searchItem.path("snippet").path("thumbnails").path("high").path("url").asText());

                    // Get exact video view count
                    fetchVideoViews(result, videoId);
                }
            }
        } catch (Exception e) {
            log.debug("Could not fetch top video for channel [{}]: {}", channelId, e.getMessage());
        }
    }

    private void fetchVideoViews(PlatformFetchResult result, String videoId) {
        try {
            String uri = String.format("/videos?part=statistics&id=%s&key=%s", videoId, apiKey);
            String responseBody = restClient.get().uri(uri).retrieve().body(String.class);
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode items = root.path("items");
            if (items.isArray() && !items.isEmpty()) {
                long views = items.get(0).path("statistics").path("viewCount").asLong(0L);
                result.setTopVideoViews(views);
            }
        } catch (Exception ignored) {
        }
    }

    private void enrichWithChannelStats(PlatformFetchResult result, String channelId) {
        try {
            String uri = String.format("/channels?part=statistics&id=%s&key=%s", channelId, apiKey);
            String responseBody = restClient.get().uri(uri).retrieve().body(String.class);
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode items = root.path("items");
            if (items.isArray() && !items.isEmpty()) {
                JsonNode stats = items.get(0).path("statistics");
                long channelViews = stats.path("viewCount").asLong(0L);
                if (channelViews > 0) {
                    result.setTotalViews(channelViews);
                }
                result.setFollowersCount(stats.path("subscriberCount").asLong(0L));
                result.setVideoCount(stats.path("videoCount").asInt(0));
            }
        } catch (Exception ignored) {
        }
    }
}
