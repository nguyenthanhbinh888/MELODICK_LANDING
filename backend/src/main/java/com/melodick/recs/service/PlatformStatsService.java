package com.melodick.recs.service;

import com.melodick.recs.dto.response.ArtistStatsResponse;
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
    private final SoundCloudPublicClient soundCloudPublicClient;
    private final int cacheTtlMinutes;

    public PlatformStatsService(ArtistRepository artistRepository,
                                ArtistPlatformStatsRepository statsRepository,
                                YouTubeApiClient youTubeApiClient,
                                YouTubePublicScraper youTubePublicScraper,
                                SpotifyPublicClient spotifyPublicClient,
                                SoundCloudPublicClient soundCloudPublicClient,
                                @Value("${app.stats.cache-ttl-minutes:120}") int cacheTtlMinutes) {
        this.artistRepository = artistRepository;
        this.statsRepository = statsRepository;
        this.youTubeApiClient = youTubeApiClient;
        this.youTubePublicScraper = youTubePublicScraper;
        this.spotifyPublicClient = spotifyPublicClient;
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
                return mapToResponse(cached, artist.getName());
            }
        }

        // Fetch freshly from public platform APIs (real data only, never fake fallback)
        ArtistPlatformStats updated = refreshAndSaveStats(artist, existingOpt.orElse(null));
        return updated != null ? mapToResponse(updated, artist.getName()) : null;
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
                ArtistStatsResponse res = mapToResponse(stats, artist.getName());
                if (res != null) {
                    responseMap.put(artist.getId(), res);
                }
            } else {
                try {
                    ArtistPlatformStats refreshed = refreshAndSaveStats(artist, stats);
                    if (refreshed != null) {
                        ArtistStatsResponse res = mapToResponse(refreshed, artist.getName());
                        if (res != null) {
                            responseMap.put(artist.getId(), res);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed refreshing stats for artist [{}]: {}", artist.getName(), e.getMessage());
                    if (stats != null && !"FALLBACK".equalsIgnoreCase(stats.getSource())) {
                        ArtistStatsResponse res = mapToResponse(stats, artist.getName());
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
        PlatformFetchResult fetchResult = null;
        List<ArtistLink> links = artist.getLinks();

        // Check if artist has any valid links
        if (links == null || links.stream().noneMatch(this::isUsableLink)) {
            if (existing != null) {
                statsRepository.delete(existing);
            }
            return null;
        }

        // 1. First priority: YouTube link
        Optional<ArtistLink> ytLink = links.stream()
                .filter(l -> isUsableLink(l) && (
                        (l.getPlatform() != null && l.getPlatform().trim().equalsIgnoreCase("YouTube"))
                        || YouTubeUrlHelper.isYouTubeUrl(l.getUrl())
                ))
                .findFirst();

        if (ytLink.isPresent()) {
            String ytUrl = ytLink.get().getUrl().trim();
            // 1a. Try YouTube Data API v3 if API key available
            if (youTubeApiClient.isConfigured()) {
                fetchResult = youTubeApiClient.fetchStats(ytUrl);
            }
            // 1b. Fallback to public RSS/oEmbed scraper
            if (fetchResult == null || !fetchResult.isSuccess()) {
                fetchResult = youTubePublicScraper.fetchStats(ytUrl);
            }
        }

        // 2. Second priority: Spotify link
        if (fetchResult == null || !fetchResult.isSuccess()) {
            Optional<ArtistLink> spLink = links.stream()
                    .filter(l -> isUsableLink(l) && (
                            (l.getPlatform() != null && l.getPlatform().trim().equalsIgnoreCase("Spotify"))
                            || (l.getUrl() != null && l.getUrl().toLowerCase().contains("spotify.com"))
                    ))
                    .findFirst();

            if (spLink.isPresent()) {
                fetchResult = spotifyPublicClient.fetchStats(spLink.get().getUrl().trim());
            }
        }

        // 3. Third priority: SoundCloud link
        if (fetchResult == null || !fetchResult.isSuccess()) {
            Optional<ArtistLink> scLink = links.stream()
                    .filter(l -> isUsableLink(l) && (
                            (l.getPlatform() != null && l.getPlatform().trim().equalsIgnoreCase("SoundCloud"))
                            || (l.getUrl() != null && l.getUrl().toLowerCase().contains("soundcloud.com"))
                    ))
                    .findFirst();

            if (scLink.isPresent()) {
                fetchResult = soundCloudPublicClient.fetchStats(scLink.get().getUrl().trim());
            }
        }

        if (fetchResult != null && fetchResult.isSuccess()) {
            ArtistPlatformStats entity = existing != null ? existing : new ArtistPlatformStats();
            entity.setArtist(artist);
            entity.setPlatform(fetchResult.getPlatform());
            entity.setChannelTitle(fetchResult.getChannelTitle());
            entity.setTotalViews(fetchResult.getTotalViews());
            entity.setFollowersCount(fetchResult.getFollowersCount());
            entity.setVideoCount(fetchResult.getVideoCount());
            entity.setTopVideoId(fetchResult.getTopVideoId());
            entity.setTopVideoTitle(fetchResult.getTopVideoTitle());
            entity.setTopVideoUrl(fetchResult.getTopVideoUrl());
            entity.setTopVideoThumbnail(fetchResult.getTopVideoThumbnail());
            entity.setTopVideoViews(fetchResult.getTopVideoViews());
            entity.setTopVideoEmbedUrl(fetchResult.getTopVideoEmbedUrl());
            entity.setSource(fetchResult.getSource());
            entity.setLastFetchedAt(LocalDateTime.now());
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

        // TopVideo is strictly for verified playable YouTube videos (never synthetic fallbacks or audio tracks)
        if ("YouTube".equalsIgnoreCase(entity.getPlatform()) &&
                StringUtils.hasText(entity.getTopVideoId()) &&
                !isSyntheticVideoId(entity.getTopVideoId())) {
            String embedUrl = StringUtils.hasText(entity.getTopVideoEmbedUrl()) ?
                    entity.getTopVideoEmbedUrl() : YouTubeUrlHelper.buildEmbedUrl(entity.getTopVideoId());
            TopVideoResponse topVideo = new TopVideoResponse();
            topVideo.setVideoId(entity.getTopVideoId());
            topVideo.setTitle(StringUtils.hasText(entity.getTopVideoTitle()) ? entity.getTopVideoTitle() : "Video nổi bật");
            topVideo.setUrl(StringUtils.hasText(entity.getTopVideoUrl()) ? entity.getTopVideoUrl() : YouTubeUrlHelper.buildWatchUrl(entity.getTopVideoId()));
            topVideo.setThumbnailUrl(StringUtils.hasText(entity.getTopVideoThumbnail()) ? entity.getTopVideoThumbnail() : YouTubeUrlHelper.buildThumbnailUrl(entity.getTopVideoId()));
            topVideo.setViewCount(entity.getTopVideoViews());
            if (entity.getTopVideoViews() > 0) {
                topVideo.setFormattedViews(ArtistStatsResponse.formatMetricCount(entity.getTopVideoViews()) + " lượt xem");
            } else {
                topVideo.setFormattedViews(null);
            }
            topVideo.setEmbedUrl(embedUrl);
            topVideo.setPlatform(entity.getPlatform());
            res.setTopVideo(topVideo);
        } else {
            res.setTopVideo(null);
        }

        return res;
    }
}
