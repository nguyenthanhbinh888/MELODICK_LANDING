package com.melodick.recs.service.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SpotifyPublicClient {

    private static final Logger log = LoggerFactory.getLogger(SpotifyPublicClient.class);
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

    private static final Pattern ARTIST_ID_PATTERN = Pattern.compile("spotify\\.com\\/artist\\/([a-zA-Z0-9]+)");
    private static final Pattern INITIAL_STATE_PATTERN = Pattern.compile("id=[\"']initialState[\"'][^>]*>([^<]+)<\\/script>");
    private static final Pattern PLAYCOUNT_PATTERN = Pattern.compile("\"play[Cc]ount\"\\s*:\\s*\"?(\\d+)\"?");
    private static final Pattern FOLLOWERS_PATTERN = Pattern.compile("\"(?:followers|totalFollowers)\"\\s*:\\s*\"?(\\d+)\"?");
    private static final Pattern MONTHLY_LISTENERS_PATTERN = Pattern.compile("\"monthlyListeners\"\\s*:\\s*\"?(\\d+)\"?");
    private static final Pattern ARTIST_NAME_PATTERN = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");

    private final String clientId;
    private final String clientSecret;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    private String cachedAccessToken;
    private Instant tokenExpiresAt = Instant.EPOCH;

    public SpotifyPublicClient(@Value("${app.spotify.client-id:}") String clientId,
                               @Value("${app.spotify.client-secret:}") String clientSecret,
                               ObjectMapper objectMapper) {
        this.clientId = clientId != null ? clientId.trim() : "";
        this.clientSecret = clientSecret != null ? clientSecret.trim() : "";
        this.objectMapper = objectMapper;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(4));
        factory.setReadTimeout(Duration.ofSeconds(4));

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
                .build();
    }

    public boolean isApiConfigured() {
        return StringUtils.hasText(clientId) && StringUtils.hasText(clientSecret);
    }

    public PlatformFetchResult fetchStats(String linkUrl) {
        if (!StringUtils.hasText(linkUrl)) {
            return PlatformFetchResult.failed("Spotify", "SPOTIFY_OEMBED");
        }

        String artistId = extractArtistId(linkUrl);

        // 1. First priority: Public Artist Profile HTML scraper (extracts real track playcounts & calculates discography average)
        if (StringUtils.hasText(artistId)) {
            PlatformFetchResult scraperResult = fetchFromPublicProfileHtml(artistId, linkUrl);
            if (scraperResult != null && scraperResult.isSuccess()) {
                return scraperResult;
            }
        }

        // 2. Second priority: Spotify Web API if configured
        if (isApiConfigured()) {
            PlatformFetchResult apiResult = fetchWithWebApi(linkUrl);
            if (apiResult != null && apiResult.isSuccess()) {
                return apiResult;
            }
        }

        // 3. Fallback: Spotify oEmbed (Public, No Key)
        return fetchWithOEmbed(linkUrl);
    }

    public PlatformFetchResult fetchFromPublicProfileHtml(String artistId, String linkUrl) {
        try {
            String profileUrl = "https://open.spotify.com/artist/" + artistId;
            String html = restClient.get()
                    .uri(profileUrl)
                    .header(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .retrieve()
                    .body(String.class);

            if (!StringUtils.hasText(html)) {
                return null;
            }

            return parseProfilePayload(html, linkUrl, artistId);
        } catch (Exception e) {
            log.debug("Spotify public HTML scraping failed for [{}]: {}", linkUrl, e.getMessage());
            return null;
        }
    }

    /**
     * Parses Spotify public artist profile HTML and initialState JSON payload:
     * Scans all tracks across the artist discography and calculates the true mathematical average play count:
     * avgStreams = totalStreams / trackCount.
     */
    public PlatformFetchResult parseProfilePayload(String html, String linkUrl, String artistId) {
        if (!StringUtils.hasText(html)) {
            return null;
        }

        String payload = html;
        Matcher stateMatcher = INITIAL_STATE_PATTERN.matcher(html);
        if (stateMatcher.find()) {
            try {
                byte[] decoded = Base64.getDecoder().decode(stateMatcher.group(1).trim());
                payload = new String(decoded, StandardCharsets.UTF_8);
            } catch (Exception ignored) {
            }
        }

        long totalStreams = 0L;
        int trackCount = 0;
        long monthlyListeners = 0L;
        long followers = 0L;
        String artistName = null;

        Matcher playMatcher = PLAYCOUNT_PATTERN.matcher(payload);
        while (playMatcher.find()) {
            try {
                long plays = Long.parseLong(playMatcher.group(1));
                if (plays > 0) {
                    totalStreams += plays;
                    trackCount++;
                }
            } catch (NumberFormatException ignored) {}
        }

        Matcher listenersMatcher = MONTHLY_LISTENERS_PATTERN.matcher(payload);
        if (listenersMatcher.find()) {
            try {
                monthlyListeners = Long.parseLong(listenersMatcher.group(1));
            } catch (NumberFormatException ignored) {}
        }

        Matcher followersMatcher = FOLLOWERS_PATTERN.matcher(payload);
        if (followersMatcher.find()) {
            try {
                followers = Long.parseLong(followersMatcher.group(1));
            } catch (NumberFormatException ignored) {}
        }

        Matcher nameMatcher = ARTIST_NAME_PATTERN.matcher(payload);
        if (nameMatcher.find()) {
            artistName = nameMatcher.group(1);
        }

        if (trackCount > 0 || monthlyListeners > 0 || followers > 0) {
            long avgStreams = trackCount > 0 ? (totalStreams / trackCount) : 0L;
            long primaryFollowers = monthlyListeners > 0 ? monthlyListeners : followers;

            PlatformFetchResult result = new PlatformFetchResult();
            result.setPlatform("Spotify");
            result.setSource("SPOTIFY_PUBLIC");
            result.setSuccess(true);
            result.setChannelTitle(StringUtils.hasText(artistName) ? artistName : "Spotify Artist");
            result.setTopVideoUrl(linkUrl);
            result.setTopVideoEmbedUrl("https://open.spotify.com/embed/artist/" + (artistId != null ? artistId : ""));
            result.setSpotifyAvgStreams(avgStreams);
            result.setSpotifyTotalStreams(totalStreams);
            result.setSpotifyTrackCount(trackCount);
            result.setSpotifyFollowers(primaryFollowers);
            result.setFollowersCount(primaryFollowers);
            result.setTotalViews(totalStreams);
            result.setTopVideoViews(avgStreams);
            result.setVideoCount(trackCount);
            return result;
        }

        return null;
    }

    private PlatformFetchResult fetchWithOEmbed(String linkUrl) {
        try {
            String oembedUrl = "https://open.spotify.com/oembed?url=" + linkUrl;
            String response = restClient.get()
                    .uri(oembedUrl)
                    .retrieve()
                    .body(String.class);

            if (StringUtils.hasText(response)) {
                JsonNode node = objectMapper.readTree(response);
                PlatformFetchResult result = new PlatformFetchResult();
                result.setPlatform("Spotify");
                result.setSource("OEMBED");
                result.setSuccess(true);
                String title = node.path("title").asText("Spotify Artist");
                result.setChannelTitle(title);
                result.setTopVideoTitle(title);
                result.setTopVideoThumbnail(node.path("thumbnail_url").asText());
                result.setTopVideoUrl(linkUrl);

                String iframeUrl = node.path("iframe_url").asText();
                if (!StringUtils.hasText(iframeUrl)) {
                    iframeUrl = linkUrl.replace("open.spotify.com/", "open.spotify.com/embed/");
                }
                result.setTopVideoEmbedUrl(iframeUrl);
                result.setTotalViews(0L);
                result.setTopVideoViews(0L);
                result.setSpotifyAvgStreams(0L);
                result.setFollowersCount(0L);
                result.setVideoCount(0);
                return result;
            }
        } catch (Exception e) {
            log.debug("Spotify oEmbed failed for [{}]: {}", linkUrl, e.getMessage());
        }
        return PlatformFetchResult.failed("Spotify", "SPOTIFY_OEMBED");
    }

    private PlatformFetchResult fetchWithWebApi(String linkUrl) {
        try {
            String artistId = extractArtistId(linkUrl);
            if (!StringUtils.hasText(artistId)) {
                return PlatformFetchResult.failed("Spotify", "SPOTIFY_API");
            }

            String token = getAccessToken();
            if (!StringUtils.hasText(token)) {
                return PlatformFetchResult.failed("Spotify", "SPOTIFY_API");
            }

            // Get artist details
            String artistUrl = "https://api.spotify.com/v1/artists/" + artistId;
            String artistResp = restClient.get()
                    .uri(artistUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(String.class);

            JsonNode artistNode = objectMapper.readTree(artistResp);
            PlatformFetchResult result = new PlatformFetchResult();
            result.setPlatform("Spotify");
            result.setSource("SPOTIFY_API");
            result.setSuccess(true);
            result.setChannelTitle(artistNode.path("name").asText());
            long followers = artistNode.path("followers").path("total").asLong(0L);
            result.setFollowersCount(followers);
            result.setSpotifyFollowers(followers);

            // Get top tracks
            String topTracksUrl = "https://api.spotify.com/v1/artists/" + artistId + "/top-tracks?market=VN";
            String topTracksResp = restClient.get()
                    .uri(topTracksUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(String.class);

            JsonNode tracksNode = objectMapper.readTree(topTracksResp).path("tracks");
            if (tracksNode.isArray() && !tracksNode.isEmpty()) {
                JsonNode topTrack = tracksNode.get(0);
                result.setTopVideoId(topTrack.path("id").asText());
                result.setTopVideoTitle(topTrack.path("name").asText());
                result.setTopVideoUrl(topTrack.path("external_urls").path("spotify").asText());
                result.setTopVideoEmbedUrl("https://open.spotify.com/embed/track/" + topTrack.path("id").asText());
                result.setTopVideoThumbnail(topTrack.path("album").path("images").get(0).path("url").asText());
                result.setTopVideoViews(0L);
                result.setTotalViews(0L);
                result.setSpotifyTrackCount(tracksNode.size());
            }

            return result;
        } catch (Exception e) {
            log.warn("Spotify Web API failed for [{}]: {}", linkUrl, e.getMessage());
        }
        return PlatformFetchResult.failed("Spotify", "SPOTIFY_API");
    }

    private synchronized String getAccessToken() {
        if (cachedAccessToken != null && Instant.now().isBefore(tokenExpiresAt)) {
            return cachedAccessToken;
        }

        try {
            String authHeader = "Basic " + Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "client_credentials");

            String response = restClient.post()
                    .uri("https://accounts.spotify.com/api/token")
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(String.class);

            JsonNode node = objectMapper.readTree(response);
            this.cachedAccessToken = node.path("access_token").asText();
            int expiresIn = node.path("expires_in").asInt(3600);
            this.tokenExpiresAt = Instant.now().plusSeconds(expiresIn - 60);
            return this.cachedAccessToken;
        } catch (Exception e) {
            log.warn("Failed to obtain Spotify access token: {}", e.getMessage());
            return null;
        }
    }

    public static String extractArtistId(String linkUrl) {
        if (!StringUtils.hasText(linkUrl)) return null;
        Matcher matcher = ARTIST_ID_PATTERN.matcher(linkUrl);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
