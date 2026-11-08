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
    }
}
