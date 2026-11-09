package com.melodick.recs.service;

import com.melodick.recs.dto.response.ArtistStatsResponse;
import com.melodick.recs.entity.Artist;
import com.melodick.recs.entity.ArtistLink;
import com.melodick.recs.entity.ArtistPlatformStats;
import com.melodick.recs.repository.ArtistPlatformStatsRepository;
import com.melodick.recs.repository.ArtistRepository;
import com.melodick.recs.service.platform.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlatformStatsServiceTest {

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private ArtistPlatformStatsRepository statsRepository;

    @Mock
    private YouTubeApiClient youTubeApiClient;

    @Mock
    private YouTubePublicScraper youTubePublicScraper;

    @Mock
    private SpotifyPublicClient spotifyPublicClient;

    @Mock
    private TikTokPublicClient tikTokPublicClient;

    @Mock
    private SoundCloudPublicClient soundCloudPublicClient;

    private PlatformStatsService platformStatsService;
    private Artist artist;

    @BeforeEach
    void setUp() {
        platformStatsService = new PlatformStatsService(
                artistRepository,
                statsRepository,
                youTubeApiClient,
                youTubePublicScraper,
                spotifyPublicClient,
                tikTokPublicClient,
                soundCloudPublicClient,
                120
        );

        artist = new Artist();
        artist.setId(1L);
        artist.setName("MIRAGEMFXNK");
        artist.addLink(new ArtistLink("YouTube", "https://www.youtube.com/@miragemfxnk", 1));
    }

    @Test
    void getArtistStats_CachedValid_ReturnsCachedWithoutExternalCall() {
        ArtistPlatformStats cached = new ArtistPlatformStats();
        cached.setArtist(artist);
        cached.setPlatform("YouTube");
        cached.setChannelTitle("MIRAGEMFXNK Official");
        cached.setTotalViews(1840000L);
        cached.setFollowersCount(42000L);
        cached.setTopVideoId("nightDriftVid");
        cached.setTopVideoTitle("NIGHT DRIFT");
        cached.setTopVideoViews(1200000L);
        cached.setSource("YOUTUBE_API");
        cached.setLastFetchedAt(LocalDateTime.now().minusMinutes(30)); // 30 mins old, valid within 120 mins

        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(statsRepository.findByArtistId(1L)).thenReturn(Optional.of(cached));

        ArtistStatsResponse response = platformStatsService.getArtistStats(1L, false);

        assertNotNull(response);
        assertEquals(1840000L, response.getTotalViews());
        assertEquals("MIRAGEMFXNK Official", response.getChannelTitle());
        assertEquals(1200000L, response.getTopVideoViews());
        assertEquals("1.2M lượt xem", response.getFormattedTopVideoViews());
        assertNotNull(response.getTopVideo());
        assertEquals("NIGHT DRIFT", response.getTopVideo().getTitle());
        assertEquals(1200000L, response.getTopVideo().getViewCount());
        assertEquals("1.2M lượt xem", response.getTopVideo().getFormattedViews());

        // Verify no external scraper/api called
        verifyNoInteractions(youTubeApiClient);
        verifyNoInteractions(youTubePublicScraper);
    }

    @Test
    void getArtistStats_NoCache_CallsPublicScraperWhenNoApiKey() {
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(statsRepository.findByArtistId(1L)).thenReturn(Optional.empty());
        when(youTubeApiClient.isConfigured()).thenReturn(false);

        PlatformFetchResult scraperResult = new PlatformFetchResult();
        scraperResult.setPlatform("YouTube");
        scraperResult.setChannelTitle("MIRAGEMFXNK Scraped");
        scraperResult.setTotalViews(2500000L);
        scraperResult.setFollowersCount(50000L);
        scraperResult.setTopVideoId("testVid123");
        scraperResult.setTopVideoTitle("Hottest Track");
        scraperResult.setTopVideoViews(1800000L);
        scraperResult.setSource("YOUTUBE_RSS");
        scraperResult.setSuccess(true);

        when(youTubePublicScraper.fetchStats("https://www.youtube.com/@miragemfxnk")).thenReturn(scraperResult);
        when(statsRepository.save(any(ArtistPlatformStats.class))).thenAnswer(i -> i.getArgument(0));

        ArtistStatsResponse response = platformStatsService.getArtistStats(1L, false);

        assertNotNull(response);
        assertEquals(2500000L, response.getTotalViews());
        assertEquals("MIRAGEMFXNK Scraped", response.getChannelTitle());
        assertEquals("YOUTUBE_RSS", response.getSource());
        assertNotNull(response.getTopVideo());
        assertEquals("testVid123", response.getTopVideo().getVideoId());

        verify(statsRepository).save(any(ArtistPlatformStats.class));
    }

    @Test
    void getArtistStats_WhenScrapingFails_ReturnsNullAndDoesNotGenerateFakeData() {
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(statsRepository.findByArtistId(1L)).thenReturn(Optional.empty());
        when(youTubeApiClient.isConfigured()).thenReturn(false);
        when(youTubePublicScraper.fetchStats(anyString())).thenReturn(PlatformFetchResult.failed("YouTube", "YOUTUBE_RSS"));

        ArtistStatsResponse response = platformStatsService.getArtistStats(1L, false);

        assertNull(response, "Stats should be null when scraping fails — no fake data should be generated");
        verify(statsRepository, never()).save(any(ArtistPlatformStats.class));
    }

    @Test
    void getArtistStats_WhenArtistHasNoLinks_ReturnsNullWithoutScraping() {
        Artist emptyLinksArtist = new Artist();
        emptyLinksArtist.setId(2L);
        emptyLinksArtist.setName("SPIDEX");

        when(artistRepository.findById(2L)).thenReturn(Optional.of(emptyLinksArtist));
        when(statsRepository.findByArtistId(2L)).thenReturn(Optional.empty());

        ArtistStatsResponse response = platformStatsService.getArtistStats(2L, false);

        assertNull(response, "Artist without links must have null stats");
        verifyNoInteractions(youTubePublicScraper);
        verifyNoInteractions(spotifyPublicClient);
        verifyNoInteractions(soundCloudPublicClient);
        verify(statsRepository, never()).save(any(ArtistPlatformStats.class));
    }

    @Test
    void getArtistStats_WhenArtistHasOnlyBlankOrHashLinks_ReturnsNullWithoutScraping() {
        Artist blankLinksArtist = new Artist();
        blankLinksArtist.setId(3L);
        blankLinksArtist.setName("Bro");
        blankLinksArtist.addLink(new ArtistLink("Spotify", "", 1));
        blankLinksArtist.addLink(new ArtistLink("YouTube", "#", 2));
        blankLinksArtist.addLink(new ArtistLink("SoundCloud", "   ", 3));

        when(artistRepository.findById(3L)).thenReturn(Optional.of(blankLinksArtist));
        when(statsRepository.findByArtistId(3L)).thenReturn(Optional.empty());

        ArtistStatsResponse response = platformStatsService.getArtistStats(3L, false);

        assertNull(response, "Artist with only empty or '#' links must have null stats");
        verifyNoInteractions(youTubePublicScraper);
        verifyNoInteractions(spotifyPublicClient);
        verifyNoInteractions(soundCloudPublicClient);
        verify(statsRepository, never()).save(any(ArtistPlatformStats.class));
    }

    @Test
    void getArtistStats_WhenYouTubeScrapesZeroViews_DoesNotSetFormattedViews() {
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(statsRepository.findByArtistId(1L)).thenReturn(Optional.empty());
        when(youTubeApiClient.isConfigured()).thenReturn(false);

        PlatformFetchResult scraperResult = new PlatformFetchResult();
        scraperResult.setPlatform("YouTube");
        scraperResult.setChannelTitle("MIRAGEMFXNK Scraped");
        scraperResult.setTotalViews(0L);
        scraperResult.setFollowersCount(0L);
        scraperResult.setTopVideoId("realVid123");
        scraperResult.setTopVideoTitle("Hottest Track");
        scraperResult.setTopVideoViews(0L);
        scraperResult.setSource("YOUTUBE_RSS");
        scraperResult.setSuccess(true);

        when(youTubePublicScraper.fetchStats("https://www.youtube.com/@miragemfxnk")).thenReturn(scraperResult);
        when(statsRepository.save(any(ArtistPlatformStats.class))).thenAnswer(i -> i.getArgument(0));

        ArtistStatsResponse response = platformStatsService.getArtistStats(1L, false);

        assertNotNull(response);
        assertEquals(0L, response.getTotalViews());
        assertNull(response.getFormattedViews(), "Zero total views must have null formattedViews");
        assertEquals(0L, response.getTopVideoViews());
        assertNull(response.getFormattedTopVideoViews(), "Zero video views must have null formattedTopVideoViews");
        assertNotNull(response.getTopVideo());
        assertNull(response.getTopVideo().getFormattedViews(), "Zero video views must have null formattedViews");
    }

    @Test
    void getStatsForArtists_SafelyHandlesDuplicateAndCaches() {
        ArtistPlatformStats cached = new ArtistPlatformStats();
        cached.setArtist(artist);
        cached.setPlatform("YouTube");
        cached.setTotalViews(50000L);
        cached.setSource("YOUTUBE_API");
        cached.setLastFetchedAt(LocalDateTime.now().minusMinutes(10));

        when(statsRepository.findAllByArtistIdIn(List.of(1L))).thenReturn(List.of(cached));

        Map<Long, ArtistStatsResponse> result = platformStatsService.getStatsForArtists(List.of(artist));

        assertNotNull(result);
        assertTrue(result.containsKey(1L));
        assertEquals(50000L, result.get(1L).getTotalViews());
    }

    @Test
    void formatMetricCount_FormatsProperly() {
        assertEquals("1.25M", ArtistStatsResponse.formatMetricCount(1250000L));
        assertEquals("45.2K", ArtistStatsResponse.formatMetricCount(45200L));
        assertEquals("950", ArtistStatsResponse.formatMetricCount(950L));
        assertEquals("2.5B", ArtistStatsResponse.formatMetricCount(2500000000L));
        assertEquals("0", ArtistStatsResponse.formatMetricCount(0L));
        assertEquals("0", ArtistStatsResponse.formatMetricCount(null));
        // Boundary rounding checks: must never produce "1000K" or "1000M"
        assertEquals("999.9K", ArtistStatsResponse.formatMetricCount(999949L));
        assertEquals("1M", ArtistStatsResponse.formatMetricCount(999950L));
        assertEquals("1M", ArtistStatsResponse.formatMetricCount(999999L));
        assertEquals("999.95M", ArtistStatsResponse.formatMetricCount(999949999L));
        assertEquals("1B", ArtistStatsResponse.formatMetricCount(999950000L));
        assertEquals("14.5B", ArtistStatsResponse.formatMetricCount(14500000000L));
    }

    @Test
    void getArtistStats_WithAllThreePlatforms_ReturnsThreeBadges() {
        Artist tripleArtist = new Artist();
        tripleArtist.setId(10L);
        tripleArtist.setName("PHONK_MASTER");
        tripleArtist.addLink(new ArtistLink("YouTube", "https://youtube.com/@phonkmaster", 1));
        tripleArtist.addLink(new ArtistLink("Spotify", "https://open.spotify.com/artist/phonk123", 2));
        tripleArtist.addLink(new ArtistLink("TikTok", "https://www.tiktok.com/@phonkmaster", 3));

        when(artistRepository.findById(10L)).thenReturn(Optional.of(tripleArtist));
        when(statsRepository.findByArtistId(10L)).thenReturn(Optional.empty());
        when(youTubeApiClient.isConfigured()).thenReturn(false);

        // Mock YouTube result
        PlatformFetchResult ytResult = new PlatformFetchResult();
        ytResult.setPlatform("YouTube");
        ytResult.setSource("YOUTUBE_RSS");
        ytResult.setTopVideoId("bestPhonkVid");
        ytResult.setTopVideoTitle("DRIFT KING");
        ytResult.setTopVideoViews(1500000L); // 1.5M views
        ytResult.setTopVideoUrl("https://youtube.com/watch?v=bestPhonkVid");
        ytResult.setTotalViews(3000000L);
        ytResult.setSuccess(true);
        when(youTubePublicScraper.fetchStats("https://youtube.com/@phonkmaster")).thenReturn(ytResult);

        // Mock Spotify result: average streams across tracks
        PlatformFetchResult spResult = new PlatformFetchResult();
        spResult.setPlatform("Spotify");
        spResult.setSource("SPOTIFY_PUBLIC");
        spResult.setSpotifyAvgStreams(85400L); // 85.4K avg streams
        spResult.setSpotifyTotalStreams(427000L);
        spResult.setSpotifyTrackCount(5);
        spResult.setSpotifyFollowers(12000L);
        spResult.setSuccess(true);
        when(spotifyPublicClient.fetchStats("https://open.spotify.com/artist/phonk123")).thenReturn(spResult);

        // Mock TikTok result: followers count ("lượt flow")
        PlatformFetchResult ttResult = new PlatformFetchResult();
        ttResult.setPlatform("TikTok");
        ttResult.setSource("TIKTOK_PUBLIC");
        ttResult.setTiktokFollowers(42800L); // 42.8K followers
        ttResult.setTiktokLikes(210000L);
        ttResult.setTiktokHandle("@phonkmaster");
        ttResult.setSuccess(true);
        when(tikTokPublicClient.fetchStats("https://www.tiktok.com/@phonkmaster")).thenReturn(ttResult);

        when(statsRepository.save(any(ArtistPlatformStats.class))).thenAnswer(i -> i.getArgument(0));

        ArtistStatsResponse response = platformStatsService.getArtistStats(10L, false);

        assertNotNull(response);
        assertEquals(3, response.getBadges().size(), "Must generate exactly 3 badges for 3 attached platforms");

        // Verify YouTube badge
        var ytBadge = response.getBadges().stream().filter(b -> "YouTube".equals(b.getPlatform())).findFirst().orElse(null);
        assertNotNull(ytBadge);
        assertEquals(1500000L, ytBadge.getValue());
        assertEquals("1.5M view", ytBadge.getFormattedBadge());

        // Verify Spotify badge
        var spBadge = response.getBadges().stream().filter(b -> "Spotify".equals(b.getPlatform())).findFirst().orElse(null);
        assertNotNull(spBadge);
        assertEquals(85400L, spBadge.getValue());
        assertEquals("85.4K nghe TB", spBadge.getFormattedBadge());

        // Verify TikTok badge
        var ttBadge = response.getBadges().stream().filter(b -> "TikTok".equals(b.getPlatform())).findFirst().orElse(null);
        assertNotNull(ttBadge);
        assertEquals(42800L, ttBadge.getValue());
        assertEquals("42.8K flow", ttBadge.getFormattedBadge());
    }

    @Test
    void getArtistStats_WithTwoPlatforms_ReturnsTwoBadges() {
        Artist dualArtist = new Artist();
        dualArtist.setId(11L);
        dualArtist.setName("DUAL_PRODUCER");
        dualArtist.addLink(new ArtistLink("Spotify", "https://open.spotify.com/artist/dual123", 1));
        dualArtist.addLink(new ArtistLink("TikTok", "https://www.tiktok.com/@dualtok", 2));

        when(artistRepository.findById(11L)).thenReturn(Optional.of(dualArtist));
        when(statsRepository.findByArtistId(11L)).thenReturn(Optional.empty());

        PlatformFetchResult spResult = new PlatformFetchResult();
        spResult.setPlatform("Spotify");
        spResult.setSource("SPOTIFY_PUBLIC");
        spResult.setSpotifyAvgStreams(120000L);
        spResult.setSpotifyTrackCount(10);
        spResult.setSuccess(true);
        when(spotifyPublicClient.fetchStats("https://open.spotify.com/artist/dual123")).thenReturn(spResult);

        PlatformFetchResult ttResult = new PlatformFetchResult();
        ttResult.setPlatform("TikTok");
        ttResult.setSource("TIKTOK_PUBLIC");
        ttResult.setTiktokFollowers(65000L);
        ttResult.setSuccess(true);
        when(tikTokPublicClient.fetchStats("https://www.tiktok.com/@dualtok")).thenReturn(ttResult);

        when(statsRepository.save(any(ArtistPlatformStats.class))).thenAnswer(i -> i.getArgument(0));

        ArtistStatsResponse response = platformStatsService.getArtistStats(11L, false);

        assertNotNull(response);
        assertEquals(2, response.getBadges().size(), "Must generate exactly 2 badges when 2 platforms attached");
        assertEquals("Spotify", response.getBadges().get(0).getPlatform());
        assertEquals("TikTok", response.getBadges().get(1).getPlatform());
    }

    @Test
    void getArtistStats_WithOnePlatform_ReturnsSingleBadge() {
        Artist singleArtist = new Artist();
        singleArtist.setId(12L);
        singleArtist.setName("TIKTOK_STAR");
        singleArtist.addLink(new ArtistLink("TikTok", "https://www.tiktok.com/@tiktokstar", 1));

        when(artistRepository.findById(12L)).thenReturn(Optional.of(singleArtist));
        when(statsRepository.findByArtistId(12L)).thenReturn(Optional.empty());

        PlatformFetchResult ttResult = new PlatformFetchResult();
        ttResult.setPlatform("TikTok");
        ttResult.setSource("TIKTOK_PUBLIC");
        ttResult.setTiktokFollowers(184000L);
        ttResult.setSuccess(true);
        when(tikTokPublicClient.fetchStats("https://www.tiktok.com/@tiktokstar")).thenReturn(ttResult);

        when(statsRepository.save(any(ArtistPlatformStats.class))).thenAnswer(i -> i.getArgument(0));

        ArtistStatsResponse response = platformStatsService.getArtistStats(12L, false);

        assertNotNull(response);
        assertEquals("TikTok", response.getPlatform(), "Platform must accurately reflect TikTok, not default to YouTube");
        assertEquals(1, response.getBadges().size(), "Must generate exactly 1 badge when 1 platform attached");
        assertEquals("184K flow", response.getBadges().get(0).getFormattedBadge());
    }

    @Test
    void getArtistStats_CachedWithUnlinkedPlatforms_OnlyRendersBadgesForCurrentlyLinkedPlatforms() {
        // Artist currently ONLY has YouTube linked
        Artist ytOnlyArtist = new Artist();
        ytOnlyArtist.setId(15L);
        ytOnlyArtist.setName("SOLO_YT");
        ytOnlyArtist.addLink(new ArtistLink("YouTube", "https://youtube.com/@soloyt", 1));

        // DB entity still has old stats for Spotify and TikTok from earlier when those links were attached
        ArtistPlatformStats cached = new ArtistPlatformStats();
        cached.setArtist(ytOnlyArtist);
        cached.setPlatform("YouTube");
        cached.setChannelTitle("SOLO_YT Channel");
        cached.setTopVideoId("topVidSolo");
        cached.setTopVideoTitle("Solo Video");
        cached.setTopVideoViews(800000L); // 800K
        cached.setTopVideoUrl("https://youtube.com/watch?v=topVidSolo");
        cached.setSpotifyAvgStreams(150000L); // Old Spotify stream count
        cached.setTiktokFollowers(45000L);    // Old TikTok follower count
        cached.setSource("REAL_MULTI_PLATFORM");
        cached.setLastFetchedAt(LocalDateTime.now().minusMinutes(10));

        when(artistRepository.findById(15L)).thenReturn(Optional.of(ytOnlyArtist));
        when(statsRepository.findByArtistId(15L)).thenReturn(Optional.of(cached));

        ArtistStatsResponse response = platformStatsService.getArtistStats(15L, false);

        assertNotNull(response);
        // CRITICAL CHECK: Badges must ONLY contain YouTube! Unlinked Spotify and TikTok MUST NOT be rendered
        assertEquals(1, response.getBadges().size(), "Must only render 1 badge because only YouTube is currently linked");
        assertEquals("YouTube", response.getBadges().get(0).getPlatform());
        assertEquals("800K view", response.getBadges().get(0).getFormattedBadge());
    }
}
