package com.melodick.recs.service.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TikTokPublicClientTest {

    private TikTokPublicClient client;

    @BeforeEach
    void setUp() {
        client = new TikTokPublicClient(new ObjectMapper());
    }

    @Test
    void isTikTokUrl_DetectsValidUrls() {
        assertTrue(TikTokPublicClient.isTikTokUrl("https://www.tiktok.com/@producer_mfxnk"));
        assertTrue(TikTokPublicClient.isTikTokUrl("https://tiktok.com/@phonk?lang=en"));
        assertFalse(TikTokPublicClient.isTikTokUrl("https://youtube.com/@phonk"));
        assertFalse(TikTokPublicClient.isTikTokUrl(null));
        assertFalse(TikTokPublicClient.isTikTokUrl(""));
    }

    @Test
    void extractHandle_ExtractsUsernameAccurately() {
        assertEquals("@miragemfxnk", TikTokPublicClient.extractHandle("https://www.tiktok.com/@miragemfxnk"));
        assertEquals("@411n_official", TikTokPublicClient.extractHandle("https://tiktok.com/@411n_official?lang=en"));
        assertNull(TikTokPublicClient.extractHandle("https://www.tiktok.com/explore"));
        assertNull(TikTokPublicClient.extractHandle(null));
    }

    @Test
    void parseCompactMetric_ConvertsFormattedStringsAccurately() {
        assertEquals(42800L, TikTokPublicClient.parseCompactMetric("42.8K"));
        assertEquals(12500L, TikTokPublicClient.parseCompactMetric("12,5K")); // Vietnamese/European decimal comma
        assertEquals(1200000L, TikTokPublicClient.parseCompactMetric("1.2M"));
        assertEquals(1200000L, TikTokPublicClient.parseCompactMetric("1,2M")); // Decimal comma
        assertEquals(2500000000L, TikTokPublicClient.parseCompactMetric("2.5B"));
        assertEquals(950L, TikTokPublicClient.parseCompactMetric("950"));
        assertEquals(18500L, TikTokPublicClient.parseCompactMetric("18,500"));
        assertEquals(0L, TikTokPublicClient.parseCompactMetric(null));
        assertEquals(0L, TikTokPublicClient.parseCompactMetric(""));
        assertEquals(0L, TikTokPublicClient.parseCompactMetric("invalid"));
    }
}
