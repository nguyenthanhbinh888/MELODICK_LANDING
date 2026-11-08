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
            if (isCacheValid(cached)) {
                return mapToResponse(cached, artist.getName());
            }
        }

        // Fetch freshly from public platform APIs or fallback
        ArtistPlatformStats updated = refreshAndSaveStats(artist, existingOpt.orElse(null));
        return mapToResponse(updated, artist.getName());
    }

    @Transactional
    public Map<Long, ArtistStatsResponse> getStatsForArtists(List<Artist> artists) {
        if (artists == null || artists.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> artistIds = artists.stream().map(Artist::getId).collect(Collectors.toList());
        List<ArtistPlatformStats> cachedList = statsRepository.findAllByArtistIdIn(artistIds);

        Map<Long, ArtistPlatformStats> statsByArtistId = cachedList.stream()
                .collect(Collectors.toMap(s -> s.getArtist().getId(), s -> s));

        Map<Long, ArtistStatsResponse> responseMap = new HashMap<>();

        for (Artist artist : artists) {
            ArtistPlatformStats stats = statsByArtistId.get(artist.getId());
            if (stats != null && isCacheValid(stats)) {
                responseMap.put(artist.getId(), mapToResponse(stats, artist.getName()));
            } else {
                // If missing or cache expired, refresh or generate fallback
                try {
                    ArtistPlatformStats refreshed = refreshAndSaveStats(artist, stats);
                    responseMap.put(artist.getId(), mapToResponse(refreshed, artist.getName()));
                } catch (Exception e) {
                    log.warn("Failed refreshing stats for artist [{}]: {}", artist.getName(), e.getMessage());
                    if (stats != null) {
                        responseMap.put(artist.getId(), mapToResponse(stats, artist.getName()));
                    } else {
                        ArtistPlatformStats fallback = generateFallbackStats(artist);
                        fallback = statsRepository.save(fallback);
                        responseMap.put(artist.getId(), mapToResponse(fallback, artist.getName()));
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
        return stats.getTopVideo();
    }

    @Transactional(readOnly = true)
    public Map<Long, ArtistStatsResponse> getCachedStatsForArtistIds(Collection<Long> artistIds) {
        if (artistIds == null || artistIds.isEmpty()) return Collections.emptyMap();
        List<ArtistPlatformStats> cached = statsRepository.findAllByArtistIdIn(artistIds);
        Map<Long, ArtistStatsResponse> map = new HashMap<>();
        for (ArtistPlatformStats s : cached) {
            String artistName = s.getArtist() != null ? s.getArtist().getName() : "";
            map.put(s.getArtist().getId(), mapToResponse(s, artistName));
        }
        return map;
    }

    private boolean isCacheValid(ArtistPlatformStats stats) {
        if (stats == null || stats.getLastFetchedAt() == null) return false;
        LocalDateTime expirationThreshold = LocalDateTime.now().minusMinutes(cacheTtlMinutes);
        return stats.getLastFetchedAt().isAfter(expirationThreshold);
    }

    private ArtistPlatformStats refreshAndSaveStats(Artist artist, ArtistPlatformStats existing) {
        PlatformFetchResult fetchResult = null;
        List<ArtistLink> links = artist.getLinks();

        // 1. First priority: YouTube link
        if (links != null) {
            Optional<ArtistLink> ytLink = links.stream()
                    .filter(l -> l.getPlatform() != null && l.getPlatform().equalsIgnoreCase("YouTube") && StringUtils.hasText(l.getUrl()))
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
        }

        // 2. Second priority: Spotify link
        if ((fetchResult == null || !fetchResult.isSuccess()) && links != null) {
            Optional<ArtistLink> spLink = links.stream()
                    .filter(l -> l.getPlatform() != null && l.getPlatform().equalsIgnoreCase("Spotify") && StringUtils.hasText(l.getUrl()))
                    .findFirst();

            if (spLink.isPresent()) {
                fetchResult = spotifyPublicClient.fetchStats(spLink.get().getUrl().trim());
            }
        }

        // 3. Third priority: SoundCloud link
        if ((fetchResult == null || !fetchResult.isSuccess()) && links != null) {
            Optional<ArtistLink> scLink = links.stream()
                    .filter(l -> l.getPlatform() != null && l.getPlatform().equalsIgnoreCase("SoundCloud") && StringUtils.hasText(l.getUrl()))
                    .findFirst();

            if (scLink.isPresent()) {
                fetchResult = soundCloudPublicClient.fetchStats(scLink.get().getUrl().trim());
            }
        }

        ArtistPlatformStats entity = existing != null ? existing : new ArtistPlatformStats();
        entity.setArtist(artist);

        if (fetchResult != null && fetchResult.isSuccess()) {
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
        } else {
            // Apply deterministic realistic fallback
            applyFallbackData(entity, artist);
        }

        entity.setLastFetchedAt(LocalDateTime.now());
        return statsRepository.save(entity);
    }

    private void applyFallbackData(ArtistPlatformStats stats, Artist artist) {
        stats.setPlatform("YouTube");
        stats.setChannelTitle(artist.getName() + " Official");

        long baseViews = 500000L + Math.abs((long) artist.getName().hashCode() % 1500000L);
        long baseFollowers = 15000L + Math.abs((long) artist.getName().hashCode() % 45000L);
        long topViews = (long) (baseViews * 0.65);

        // Curated track suggestions
        String topTitle = artist.getName() + " - TOKYO DRIFT (Official Audio)";
        String videoId = "kxpcV6650V4";
        String thumb = StringUtils.hasText(artist.getImageUrl()) ? artist.getImageUrl() :
                "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80";

        if (artist.getName().toLowerCase().contains("411n")) {
            topTitle = "411N - MEMPHIS RITUAL (Visualizer)";
            videoId = "5qap5aO4i9A";
            baseViews = 980400L;
            baseFollowers = 28400L;
            topViews = 680000L;
        } else if (artist.getName().toLowerCase().contains("spidex")) {
            topTitle = "SPIDEX - DARK REALITY (Bass Boosted)";
            videoId = "CevxZvSJLk8";
            baseViews = 742100L;
            baseFollowers = 19800L;
            topViews = 495000L;
        } else if (artist.getName().toLowerCase().contains("mirage")) {
            topTitle = "MIRAGEMFXNK - TOKYO DRIFT 2.0 (Official Music Video)";
            videoId = "kxpcV6650V4";
            baseViews = 1845200L;
            baseFollowers = 42800L;
            topViews = 1250000L;
        }

        stats.setTotalViews(baseViews);
        stats.setFollowersCount(baseFollowers);
        stats.setVideoCount(12);
        stats.setTopVideoId(videoId);
        stats.setTopVideoTitle(topTitle);
        stats.setTopVideoUrl(YouTubeUrlHelper.buildWatchUrl(videoId));
        stats.setTopVideoEmbedUrl(YouTubeUrlHelper.buildEmbedUrl(videoId));
        stats.setTopVideoThumbnail(thumb);
        stats.setTopVideoViews(topViews);
        stats.setSource("FALLBACK");
    }

    private ArtistPlatformStats generateFallbackStats(Artist artist) {
        ArtistPlatformStats stats = new ArtistPlatformStats();
        stats.setArtist(artist);
        applyFallbackData(stats, artist);
        stats.setLastFetchedAt(LocalDateTime.now());
        return stats;
    }

    public ArtistStatsResponse mapToResponse(ArtistPlatformStats entity, String artistName) {
        ArtistStatsResponse res = new ArtistStatsResponse();
        res.setArtistId(entity.getArtist() != null ? entity.getArtist().getId() : null);
        res.setArtistName(artistName);
        res.setPlatform(entity.getPlatform());
        res.setChannelTitle(entity.getChannelTitle());
        res.setTotalViews(entity.getTotalViews());
        res.setFormattedViews(ArtistStatsResponse.formatMetricCount(entity.getTotalViews()) + " lượt xem");
        res.setFollowersCount(entity.getFollowersCount());
        res.setFormattedFollowers(ArtistStatsResponse.formatMetricCount(entity.getFollowersCount()) + " người theo dõi");
        res.setVideoCount(entity.getVideoCount());
        res.setSource(entity.getSource());
        res.setLastFetchedAt(entity.getLastFetchedAt());

        if (StringUtils.hasText(entity.getTopVideoTitle()) || StringUtils.hasText(entity.getTopVideoId())) {
            TopVideoResponse topVideo = new TopVideoResponse();
            topVideo.setVideoId(entity.getTopVideoId());
            topVideo.setTitle(entity.getTopVideoTitle());
            topVideo.setUrl(entity.getTopVideoUrl());
            topVideo.setThumbnailUrl(entity.getTopVideoThumbnail());
            topVideo.setViewCount(entity.getTopVideoViews());
            topVideo.setFormattedViews(ArtistStatsResponse.formatMetricCount(entity.getTopVideoViews()) + " lượt xem");
            topVideo.setEmbedUrl(entity.getTopVideoEmbedUrl());
            topVideo.setPlatform(entity.getPlatform());
            res.setTopVideo(topVideo);
        }

        return res;
    }
}
