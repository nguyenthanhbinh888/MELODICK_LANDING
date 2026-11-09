package com.melodick.recs.service;

import com.melodick.recs.dto.response.ArtistStatsResponse;
import com.melodick.recs.dto.response.PlatformBadgeResponse;
import com.melodick.recs.dto.response.TopVideoResponse;
import com.melodick.recs.entity.Artist;
import com.melodick.recs.entity.ArtistLink;
import com.melodick.recs.entity.ArtistPlatformStats;
import com.melodick.recs.exception.ResourceNotFoundException;
import com.melodick.recs.repository.ArtistPlatformStatsRepository;
import com.melodick.recs.repository.ArtistRepository;
import com.melodick.recs.service.platform.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlatformStatsService {

    private static final Logger log = LoggerFactory.getLogger(PlatformStatsService.class);

    private final ArtistRepository artistRepository;
    private final ArtistPlatformStatsRepository statsRepository;
    private final YouTubeApiClient youTubeApiClient;
    private final YouTubePublicScraper youTubePublicScraper;
    private final SpotifyPublicClient spotifyPublicClient;
    private final TikTokPublicClient tikTokPublicClient;
    private final SoundCloudPublicClient soundCloudPublicClient;
    private final int cacheTtlMinutes;

    public PlatformStatsService(ArtistRepository artistRepository,
                                ArtistPlatformStatsRepository statsRepository,
                                YouTubeApiClient youTubeApiClient,
                                YouTubePublicScraper youTubePublicScraper,
                                SpotifyPublicClient spotifyPublicClient,
                                TikTokPublicClient tikTokPublicClient,
                                SoundCloudPublicClient soundCloudPublicClient,
                                @Value("${app.stats.cache-ttl-minutes:120}") int cacheTtlMinutes) {
        this.artistRepository = artistRepository;
        this.statsRepository = statsRepository;
        this.youTubeApiClient = youTubeApiClient;
        this.youTubePublicScraper = youTubePublicScraper;
        this.spotifyPublicClient = spotifyPublicClient;
        this.tikTokPublicClient = tikTokPublicClient;
        this.soundCloudPublicClient = soundCloudPublicClient;
        this.cacheTtlMinutes = cacheTtlMinutes > 0 ? cacheTtlMinutes : 120;
    }

    @Transactional
    public ArtistStatsResponse getArtistStats(Long artistId, boolean forceRefresh) {
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nghệ sĩ với ID: " + artistId));

        Optional<ArtistPlatformStats> existingOpt = statsRepository.findByArtistId(artistId);

        if (!forceRefresh && existingOpt.isPresent()) {
            ArtistPlatformStats cached = existingOpt.get();
            if (!"FALLBACK".equalsIgnoreCase(cached.getSource()) && isCacheValid(cached)) {
                return mapToResponse(cached, artist.getName(), artist.getLinks());
            }
        }

        // Fetch freshly from public platform APIs (real data only, never fake fallback)
        ArtistPlatformStats updated = refreshAndSaveStats(artist, existingOpt.orElse(null));
        return updated != null ? mapToResponse(updated, artist.getName(), artist.getLinks()) : null;
    }

    @Transactional
    public Map<Long, ArtistStatsResponse> getStatsForArtists(List<Artist> artists) {
        if (artists == null || artists.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> artistIds = artists.stream().map(Artist::getId).collect(Collectors.toList());
        List<ArtistPlatformStats> cachedList = statsRepository.findAllByArtistIdIn(artistIds);

        Map<Long, ArtistPlatformStats> statsByArtistId = cachedList.stream()
                .filter(s -> s.getArtist() != null && s.getArtist().getId() != null)
                .collect(Collectors.toMap(s -> s.getArtist().getId(), s -> s, (s1, s2) -> s1));

        Map<Long, ArtistStatsResponse> responseMap = new HashMap<>();

        for (Artist artist : artists) {
            ArtistPlatformStats stats = statsByArtistId.get(artist.getId());
            if (stats != null && !"FALLBACK".equalsIgnoreCase(stats.getSource()) && isCacheValid(stats)) {
                ArtistStatsResponse res = mapToResponse(stats, artist.getName(), artist.getLinks());
                if (res != null) {
                    responseMap.put(artist.getId(), res);
                }
            } else {
                try {
                    ArtistPlatformStats refreshed = refreshAndSaveStats(artist, stats);
                    if (refreshed != null) {
                        ArtistStatsResponse res = mapToResponse(refreshed, artist.getName(), artist.getLinks());
                        if (res != null) {
                            responseMap.put(artist.getId(), res);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed refreshing stats for artist [{}]: {}", artist.getName(), e.getMessage());
                    if (stats != null && !"FALLBACK".equalsIgnoreCase(stats.getSource())) {
                        ArtistStatsResponse res = mapToResponse(stats, artist.getName(), artist.getLinks());
                        if (res != null) {
                            responseMap.put(artist.getId(), res);
                        }
                    }
                }
            }
        }

        return responseMap;
    }

    @Transactional
    public ArtistStatsResponse forceRefreshArtistStats(Long artistId) {
        return getArtistStats(artistId, true);
    }

    @Transactional(readOnly = true)
    public TopVideoResponse getTopVideo(Long artistId) {
        ArtistStatsResponse stats = getArtistStats(artistId, false);
        return stats != null ? stats.getTopVideo() : null;
    }

    @Transactional(readOnly = true)
    public Map<Long, ArtistStatsResponse> getCachedStatsForArtistIds(Collection<Long> artistIds) {
        if (artistIds == null || artistIds.isEmpty()) return Collections.emptyMap();
        List<ArtistPlatformStats> cached = statsRepository.findAllByArtistIdIn(artistIds);
        Map<Long, ArtistStatsResponse> map = new HashMap<>();
        for (ArtistPlatformStats s : cached) {
            if (!"FALLBACK".equalsIgnoreCase(s.getSource())) {
                String artistName = s.getArtist() != null ? s.getArtist().getName() : "";
                ArtistStatsResponse res = mapToResponse(s, artistName);
                if (res != null) {
                    map.put(s.getArtist().getId(), res);
                }
            }
        }
        return map;
    }

    private boolean isCacheValid(ArtistPlatformStats stats) {
        if (stats == null || stats.getLastFetchedAt() == null) return false;
        LocalDateTime expirationThreshold = LocalDateTime.now().minusMinutes(cacheTtlMinutes);
        return stats.getLastFetchedAt().isAfter(expirationThreshold);
    }

    private boolean isUsableLink(ArtistLink link) {
        if (link == null || !StringUtils.hasText(link.getUrl())) {
            return false;
        }
        String trimmed = link.getUrl().trim();
        return !trimmed.equals("#") && !trimmed.equalsIgnoreCase("none") && !trimmed.equalsIgnoreCase("null");
    }

    private ArtistPlatformStats refreshAndSaveStats(Artist artist, ArtistPlatformStats existing) {
        List<ArtistLink> links = artist.getLinks();

        // Check if artist has any valid links
        if (links == null || links.stream().noneMatch(this::isUsableLink)) {
            if (existing != null) {
                statsRepository.delete(existing);
            }
            return null;
        }

        PlatformFetchResult ytResult = null;
        PlatformFetchResult spResult = null;
        PlatformFetchResult ttResult = null;
        PlatformFetchResult scResult = null;

        // 1. YouTube link
        Optional<ArtistLink> ytLink = links.stream()
                .filter(l -> isUsableLink(l) && (
                        (l.getPlatform() != null && l.getPlatform().trim().equalsIgnoreCase("YouTube"))
                        || YouTubeUrlHelper.isYouTubeUrl(l.getUrl())
                ))
                .findFirst();

        if (ytLink.isPresent()) {
            String ytUrl = ytLink.get().getUrl().trim();
            if (youTubeApiClient.isConfigured()) {
                ytResult = youTubeApiClient.fetchStats(ytUrl);
            }
            if (ytResult == null || !ytResult.isSuccess()) {
                ytResult = youTubePublicScraper.fetchStats(ytUrl);
            }
        }

        // 2. Spotify link
        Optional<ArtistLink> spLink = links.stream()
                .filter(l -> isUsableLink(l) && (
                        (l.getPlatform() != null && l.getPlatform().trim().equalsIgnoreCase("Spotify"))
                        || (l.getUrl() != null && l.getUrl().toLowerCase().contains("spotify.com"))
                ))
                .findFirst();

        if (spLink.isPresent()) {
            spResult = spotifyPublicClient.fetchStats(spLink.get().getUrl().trim());
        }

        // 3. TikTok link
        Optional<ArtistLink> ttLink = links.stream()
                .filter(l -> isUsableLink(l) && (
                        (l.getPlatform() != null && l.getPlatform().trim().equalsIgnoreCase("TikTok"))
                        || TikTokPublicClient.isTikTokUrl(l.getUrl())
                ))
                .findFirst();

        if (ttLink.isPresent()) {
            ttResult = tikTokPublicClient.fetchStats(ttLink.get().getUrl().trim());
        }

        // 4. SoundCloud link
        Optional<ArtistLink> scLink = links.stream()
                .filter(l -> isUsableLink(l) && (
                        (l.getPlatform() != null && l.getPlatform().trim().equalsIgnoreCase("SoundCloud"))
                        || (l.getUrl() != null && l.getUrl().toLowerCase().contains("soundcloud.com"))
                ))
                .findFirst();

        if (scLink.isPresent()) {
            scResult = soundCloudPublicClient.fetchStats(scLink.get().getUrl().trim());
        }

        boolean hasAnySuccess = (ytResult != null && ytResult.isSuccess())
                || (spResult != null && spResult.isSuccess())
                || (ttResult != null && ttResult.isSuccess())
                || (scResult != null && scResult.isSuccess());

        if (hasAnySuccess) {
            ArtistPlatformStats entity = existing != null ? existing : new ArtistPlatformStats();
            entity.setArtist(artist);
            entity.setLastFetchedAt(LocalDateTime.now());
            String source = (ytResult != null && ytResult.isSuccess() && !"FALLBACK".equalsIgnoreCase(ytResult.getSource())) ? ytResult.getSource() :
                    ((spResult != null && spResult.isSuccess() && !"FALLBACK".equalsIgnoreCase(spResult.getSource())) ? spResult.getSource() :
                    ((ttResult != null && ttResult.isSuccess() && !"FALLBACK".equalsIgnoreCase(ttResult.getSource())) ? ttResult.getSource() : "REAL_MULTI_PLATFORM"));
            entity.setSource(source);

            // 1. YouTube Stats
            if (ytResult != null && ytResult.isSuccess()) {
                entity.setTotalViews(ytResult.getTotalViews());
                entity.setFollowersCount(ytResult.getFollowersCount());
                entity.setVideoCount(ytResult.getVideoCount());
                entity.setTopVideoId(ytResult.getTopVideoId());
                entity.setTopVideoTitle(ytResult.getTopVideoTitle());
                entity.setTopVideoUrl(ytResult.getTopVideoUrl());
                entity.setTopVideoThumbnail(ytResult.getTopVideoThumbnail());
                entity.setTopVideoViews(ytResult.getTopVideoViews());
                entity.setTopVideoEmbedUrl(ytResult.getTopVideoEmbedUrl());
            } else if (ytLink.isEmpty()) {
                // Clear stale YouTube stats if unlinked
                entity.setTotalViews(0L);
                entity.setVideoCount(0);
                entity.setTopVideoId(null);
                entity.setTopVideoTitle(null);
                entity.setTopVideoUrl(null);
                entity.setTopVideoThumbnail(null);
                entity.setTopVideoViews(0L);
                entity.setTopVideoEmbedUrl(null);
            }

            // 2. Spotify Stats
            if (spResult != null && spResult.isSuccess()) {
                entity.setSpotifyAvgStreams(spResult.getSpotifyAvgStreams());
                entity.setSpotifyTotalStreams(spResult.getSpotifyTotalStreams());
                entity.setSpotifyTrackCount(spResult.getSpotifyTrackCount());
                entity.setSpotifyFollowers(spResult.getSpotifyFollowers());

                if (entity.getTopVideoViews() == null || entity.getTopVideoViews() <= 0) {
                    entity.setTopVideoViews(spResult.getSpotifyAvgStreams());
                }
            } else if (spLink.isEmpty()) {
                // Clear stale Spotify stats if unlinked
                entity.setSpotifyAvgStreams(0L);
                entity.setSpotifyTotalStreams(0L);
                entity.setSpotifyTrackCount(0);
                entity.setSpotifyFollowers(0L);
            }

            // 3. TikTok Stats
            if (ttResult != null && ttResult.isSuccess()) {
                long ttFollowers = ttResult.getTiktokFollowers() > 0 ? ttResult.getTiktokFollowers() : ttResult.getFollowersCount();
                entity.setTiktokFollowers(ttFollowers);
                entity.setTiktokLikes(ttResult.getTiktokLikes());
                entity.setTiktokHandle(ttResult.getTiktokHandle());
            } else if (ttLink.isEmpty()) {
                // Clear stale TikTok stats if unlinked
                entity.setTiktokFollowers(0L);
                entity.setTiktokLikes(0L);
                entity.setTiktokHandle(null);
            }

            // 4. SoundCloud Stats
            if (scResult != null && scResult.isSuccess()) {
                entity.setFollowersCount(scResult.getFollowersCount());
            }

            // Resolve Primary Platform accurately
            if (ytResult != null && ytResult.isSuccess()) {
                entity.setPlatform("YouTube");
            } else if (spResult != null && spResult.isSuccess()) {
                entity.setPlatform("Spotify");
            } else if (ttResult != null && ttResult.isSuccess()) {
                entity.setPlatform("TikTok");
            } else if (scResult != null && scResult.isSuccess()) {
                entity.setPlatform("SoundCloud");
            } else if (!links.isEmpty() && StringUtils.hasText(links.get(0).getPlatform())) {
                entity.setPlatform(links.get(0).getPlatform());
            } else {
                entity.setPlatform("Artist");
            }

            // Resolve Channel Title accurately
            String resolvedTitle = null;
            if (ytResult != null && ytResult.isSuccess() && StringUtils.hasText(ytResult.getChannelTitle())) {
                resolvedTitle = ytResult.getChannelTitle();
            } else if (spResult != null && spResult.isSuccess() && StringUtils.hasText(spResult.getChannelTitle())) {
                resolvedTitle = spResult.getChannelTitle();
            } else if (ttResult != null && ttResult.isSuccess() && StringUtils.hasText(ttResult.getChannelTitle())) {
                resolvedTitle = ttResult.getChannelTitle();
            } else if (scResult != null && scResult.isSuccess() && StringUtils.hasText(scResult.getChannelTitle())) {
                resolvedTitle = scResult.getChannelTitle();
            } else {
                resolvedTitle = artist.getName();
            }
            entity.setChannelTitle(resolvedTitle);

            return statsRepository.save(entity);
        } else {
            // Real fetch failed or returned nothing: remove existing stale/fallback record and return null
            if (existing != null) {
                statsRepository.delete(existing);
            }
            return null;
        }
    }

    private boolean isSyntheticVideoId(String videoId) {
        if (!StringUtils.hasText(videoId)) return true;
        return "kxpcV6650V4".equals(videoId) || "5qap5aO4i9A".equals(videoId) || "CevxZvSJLk8".equals(videoId);
    }

    public ArtistStatsResponse mapToResponse(ArtistPlatformStats entity, String artistName) {
        return mapToResponse(entity, artistName, entity != null && entity.getArtist() != null ? entity.getArtist().getLinks() : null);
    }

    public ArtistStatsResponse mapToResponse(ArtistPlatformStats entity, String artistName, List<ArtistLink> links) {
        if (entity == null || "FALLBACK".equalsIgnoreCase(entity.getSource())) {
            return null;
        }

        ArtistStatsResponse res = new ArtistStatsResponse();
        res.setArtistId(entity.getArtist() != null ? entity.getArtist().getId() : null);
        res.setArtistName(artistName);
        res.setPlatform(entity.getPlatform());
        res.setChannelTitle(entity.getChannelTitle());
        res.setTotalViews(entity.getTotalViews());
        if (entity.getTotalViews() > 0) {
            res.setFormattedViews(ArtistStatsResponse.formatMetricCount(entity.getTotalViews()) + " lượt xem");
        } else {
            res.setFormattedViews(null);
        }

        res.setFollowersCount(entity.getFollowersCount());
        if (entity.getFollowersCount() > 0) {
            res.setFormattedFollowers(ArtistStatsResponse.formatMetricCount(entity.getFollowersCount()) + " người theo dõi");
        } else {
            res.setFormattedFollowers(null);
        }

        res.setVideoCount(entity.getVideoCount());
        res.setSource(entity.getSource());
        res.setLastFetchedAt(entity.getLastFetchedAt());

        // Spotify metrics
        Long spAvg = entity.getSpotifyAvgStreams();
        res.setSpotifyAvgStreams(spAvg != null ? spAvg : 0L);
        if (spAvg != null && spAvg > 0) {
            res.setFormattedSpotifyAvgStreams(ArtistStatsResponse.formatMetricCount(spAvg) + " nghe TB");
        } else {
            res.setFormattedSpotifyAvgStreams(null);
        }

        // TikTok metrics
        Long ttFollowers = entity.getTiktokFollowers();
        res.setTiktokFollowers(ttFollowers != null ? ttFollowers : 0L);
        if (ttFollowers != null && ttFollowers > 0) {
            res.setFormattedTikTokFollowers(ArtistStatsResponse.formatMetricCount(ttFollowers) + " flow");
        } else {
            res.setFormattedTikTokFollowers(null);
        }

        // YouTube Top Video (playable verified video, never synthetic)
        boolean isPlayableYouTube = "YouTube".equalsIgnoreCase(entity.getPlatform()) &&
                StringUtils.hasText(entity.getTopVideoId()) &&
                !isSyntheticVideoId(entity.getTopVideoId());

        if (isPlayableYouTube) {
            Long topViews = entity.getTopVideoViews();
            res.setTopVideoViews(topViews != null ? topViews : 0L);
            if (topViews != null && topViews > 0) {
                res.setFormattedTopVideoViews(ArtistStatsResponse.formatMetricCount(topViews) + " lượt xem");
            } else {
                res.setFormattedTopVideoViews(null);
            }

            String embedUrl = StringUtils.hasText(entity.getTopVideoEmbedUrl()) ?
                    entity.getTopVideoEmbedUrl() : YouTubeUrlHelper.buildEmbedUrl(entity.getTopVideoId());
            TopVideoResponse topVideo = new TopVideoResponse();
            topVideo.setVideoId(entity.getTopVideoId());
            topVideo.setTitle(StringUtils.hasText(entity.getTopVideoTitle()) ? entity.getTopVideoTitle() : "Video nổi bật");
            topVideo.setUrl(StringUtils.hasText(entity.getTopVideoUrl()) ? entity.getTopVideoUrl() : YouTubeUrlHelper.buildWatchUrl(entity.getTopVideoId()));
            topVideo.setThumbnailUrl(StringUtils.hasText(entity.getTopVideoThumbnail()) ? entity.getTopVideoThumbnail() : YouTubeUrlHelper.buildThumbnailUrl(entity.getTopVideoId()));
            topVideo.setViewCount(topViews);
            if (topViews != null && topViews > 0) {
                topVideo.setFormattedViews(ArtistStatsResponse.formatMetricCount(topViews) + " lượt xem");
            } else {
                topVideo.setFormattedViews(null);
            }
            topVideo.setEmbedUrl(embedUrl);
            topVideo.setPlatform("YouTube");
            res.setTopVideo(topVideo);
        } else {
            res.setTopVideo(null);
            res.setTopVideoViews(0L);
            res.setFormattedTopVideoViews(null);
        }

        // Multi-Platform Badges Engine: 1 of 3, 2 of 3, 3 of 3
        // Badges are ONLY displayed when the corresponding platform link is attached
        List<PlatformBadgeResponse> badges = new ArrayList<>();

        boolean checkLinks = (links != null && !links.isEmpty());
        boolean hasYtLink = !checkLinks || hasPlatformLink(links, "YouTube", "youtu.be");
        boolean hasSpLink = !checkLinks || hasPlatformLink(links, "Spotify");
        boolean hasTtLink = !checkLinks || hasPlatformLink(links, "TikTok");

        // 1. YouTube Top Video Views Badge
        if (hasYtLink && isPlayableYouTube && entity.getTopVideoViews() != null && entity.getTopVideoViews() > 0) {
            String ytUrl = findPlatformUrl(links, "YouTube", entity.getTopVideoUrl());
            String formattedVal = ArtistStatsResponse.formatMetricCount(entity.getTopVideoViews());
            String formattedBadge = formattedVal + " view";
            String tooltip = StringUtils.hasText(entity.getTopVideoTitle()) ?
                    "Lượt xem video cao nhất (" + entity.getTopVideoTitle() + "): " + String.format(Locale.US, "%,d", entity.getTopVideoViews()) + " view" :
                    "Lượt xem video cao nhất: " + String.format(Locale.US, "%,d", entity.getTopVideoViews()) + " view";
            badges.add(new PlatformBadgeResponse("YouTube", "top_views", entity.getTopVideoViews(), formattedVal, "view", formattedBadge, tooltip, ytUrl, "youtube"));
        }

        // 2. Spotify Average Streams Badge
        if (hasSpLink && spAvg != null && spAvg > 0) {
            String spUrl = findPlatformUrl(links, "Spotify", null);
            String formattedVal = ArtistStatsResponse.formatMetricCount(spAvg);
            String formattedBadge = formattedVal + " nghe TB";
            String tooltip = "Lượt nghe trung bình Spotify: " + String.format(Locale.US, "%,d", spAvg) + " stream/bài";
            badges.add(new PlatformBadgeResponse("Spotify", "avg_views", spAvg, formattedVal, "nghe TB", formattedBadge, tooltip, spUrl, "spotify"));
        }

        // 3. TikTok Followers Badge ("lượt flow")
        if (hasTtLink && ttFollowers != null && ttFollowers > 0) {
            String ttUrl = findPlatformUrl(links, "TikTok", null);
            String formattedVal = ArtistStatsResponse.formatMetricCount(ttFollowers);
            String formattedBadge = formattedVal + " flow";
            String tooltip = "Lượt flow TikTok: " + String.format(Locale.US, "%,d", ttFollowers) + " người theo dõi";
            badges.add(new PlatformBadgeResponse("TikTok", "followers", ttFollowers, formattedVal, "flow", formattedBadge, tooltip, ttUrl, "tiktok"));
        }

        res.setBadges(badges);
        return res;
    }

    private boolean hasPlatformLink(List<ArtistLink> links, String... matchers) {
        if (links == null || links.isEmpty()) return false;
        for (ArtistLink link : links) {
            if (!isUsableLink(link)) continue;
            String p = link.getPlatform() != null ? link.getPlatform().toLowerCase() : "";
            String u = link.getUrl() != null ? link.getUrl().toLowerCase() : "";
            for (String m : matchers) {
                String target = m.toLowerCase();
                if (p.contains(target) || u.contains(target)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String findPlatformUrl(List<ArtistLink> links, String platformName, String fallbackUrl) {
        if (links != null) {
            String target = platformName.toLowerCase();
            for (ArtistLink l : links) {
                if (isUsableLink(l)) {
                    String p = l.getPlatform() != null ? l.getPlatform().toLowerCase() : "";
                    String u = l.getUrl() != null ? l.getUrl().toLowerCase() : "";
                    if (p.contains(target) || u.contains(target)) {
                        return l.getUrl();
                    }
                }
            }
        }
        return fallbackUrl;
    }
}
