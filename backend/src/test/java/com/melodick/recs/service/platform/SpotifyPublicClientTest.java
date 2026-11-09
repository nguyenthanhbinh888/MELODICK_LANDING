package com.melodick.recs.service.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpotifyPublicClientTest {

    private SpotifyPublicClient client;

    @BeforeEach
    void setUp() {
        client = new SpotifyPublicClient("", "", new ObjectMapper());
    }

    @Test
    void extractArtistId_ExtractsCorrectId() {
        assertEquals("4Z8W4fKeB5YxbusRsdQVPb",
                SpotifyPublicClient.extractArtistId("https://open.spotify.com/artist/4Z8W4fKeB5YxbusRsdQVPb"));
        assertEquals("0k17h0D3J5VfsdmQ1iZtE9",
                SpotifyPublicClient.extractArtistId("https://open.spotify.com/artist/0k17h0D3J5VfsdmQ1iZtE9?si=abc123xyz"));
        assertNull(SpotifyPublicClient.extractArtistId("https://open.spotify.com/track/123456"));
        assertNull(SpotifyPublicClient.extractArtistId(null));
    }

    @Test
    void parseProfilePayload_CalculatesTrueAverageStreamsAcrossTracks() {
        // Sample HTML with 3 tracks of known playcounts: 100,000, 200,000, and 300,000
        // Expected total = 600,000; Expected average = 600,000 / 3 = 200,000
        String sampleHtml = """
                <html>
                <body>
                  <script id="initialState" type="text/plain"></script>
                  <div>
                    "name": "MIRAGEMFXNK"
                    "followers": 15000
                    "monthlyListeners": 45000
                    "playcount": "100000"
                    "playcount": "200000"
                    "playcount": "300000"
                  </div>
                </body>
                </html>
                """;

        PlatformFetchResult result = client.parseProfilePayload(sampleHtml, "https://open.spotify.com/artist/testId", "testId");

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Spotify", result.getPlatform());
        assertEquals(3, result.getSpotifyTrackCount(), "Should scan 3 tracks");
        assertEquals(600000L, result.getSpotifyTotalStreams(), "Total streams should sum to 600,000");
        assertEquals(200000L, result.getSpotifyAvgStreams(), "Average streams should be 600,000 / 3 = 200,000");
        assertEquals(45000L, result.getSpotifyFollowers(), "Monthly listeners should be captured");
    }
}
