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
        cached.setTopVideoTitle("NIGHT DRIFT");
        cached.setTopVideoViews(1200000L);
        cached.setLastFetchedAt(LocalDateTime.now().minusMinutes(30)); // 30 mins old, valid within 120 mins

        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(statsRepository.findByArtistId(1L)).thenReturn(Optional.of(cached));

        ArtistStatsResponse response = platformStatsService.getArtistStats(1L, false);

        assertNotNull(response);
        assertEquals(1840000L, response.getTotalViews());
        assertEquals("MIRAGEMFXNK Official", response.getChannelTitle());
        assertNotNull(response.getTopVideo());
        assertEquals("NIGHT DRIFT", response.getTopVideo().getTitle());

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
    void getArtistStats_OfflineFallback_GeneratesRealisticStats() {
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(statsRepository.findByArtistId(1L)).thenReturn(Optional.empty());
        when(youTubeApiClient.isConfigured()).thenReturn(false);
        when(youTubePublicScraper.fetchStats(anyString())).thenReturn(PlatformFetchResult.failed("YouTube", "YOUTUBE_RSS"));
        when(statsRepository.save(any(ArtistPlatformStats.class))).thenAnswer(i -> i.getArgument(0));

        ArtistStatsResponse response = platformStatsService.getArtistStats(1L, false);

        assertNotNull(response);
        assertTrue(response.getTotalViews() > 0);
        assertTrue(response.getFollowersCount() > 0);
        assertEquals("FALLBACK", response.getSource());
        assertNotNull(response.getTopVideo());
        assertTrue(response.getTopVideo().getTitle().contains("MIRAGEMFXNK"));
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
