package com.melodick.recs.service.platform;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class YouTubeUrlHelperTest {

    @Test
    void extractVideoId_VariousFormats() {
        // Standard watch URL
        assertEquals("kxpcV6650V4", YouTubeUrlHelper.extractVideoId("https://www.youtube.com/watch?v=kxpcV6650V4"));
        assertEquals("kxpcV6650V4", YouTubeUrlHelper.extractVideoId("http://youtube.com/watch?v=kxpcV6650V4&feature=shared"));
        assertEquals("kxpcV6650V4", YouTubeUrlHelper.extractVideoId("https://www.youtube.com/watch?feature=shared&v=kxpcV6650V4"));
        assertEquals("kxpcV6650V4", YouTubeUrlHelper.extractVideoId("https://www.youtube.com/watch?si=xyz123&v=kxpcV6650V4&t=10s"));

        // Short youtu.be URL
        assertEquals("5qap5aO4i9A", YouTubeUrlHelper.extractVideoId("https://youtu.be/5qap5aO4i9A"));
        assertEquals("5qap5aO4i9A", YouTubeUrlHelper.extractVideoId("https://youtu.be/5qap5aO4i9A?si=xyz123"));

        // Embed URL
        assertEquals("CevxZvSJLk8", YouTubeUrlHelper.extractVideoId("https://www.youtube.com/embed/CevxZvSJLk8"));

        // Shorts URL
        assertEquals("dQw4w9WgXcQ", YouTubeUrlHelper.extractVideoId("https://www.youtube.com/shorts/dQw4w9WgXcQ"));

        // Live stream URL
        assertEquals("liveVid1234", YouTubeUrlHelper.extractVideoId("https://www.youtube.com/live/liveVid1234"));

        // Direct 11-char ID
        assertEquals("dQw4w9WgXcQ", YouTubeUrlHelper.extractVideoId("dQw4w9WgXcQ"));

        // Non-video inputs
        assertNull(YouTubeUrlHelper.extractVideoId("https://www.youtube.com/@miragemfxnk"));
        assertNull(YouTubeUrlHelper.extractVideoId(""));
        assertNull(YouTubeUrlHelper.extractVideoId(null));
    }

    @Test
    void extractChannelId_VariousFormats() {
        String testChannelId = "UC1234567890abcdefghijk";
        assertEquals(testChannelId, YouTubeUrlHelper.extractChannelId("https://www.youtube.com/channel/" + testChannelId));
        assertEquals(testChannelId, YouTubeUrlHelper.extractChannelId(testChannelId));

        assertNull(YouTubeUrlHelper.extractChannelId("https://www.youtube.com/@handle"));
        assertNull(YouTubeUrlHelper.extractChannelId("invalid_channel_id"));
    }

    @Test
    void extractHandle_VariousFormats() {
        assertEquals("miragemfxnk", YouTubeUrlHelper.extractHandle("https://www.youtube.com/@miragemfxnk"));
        assertEquals("411n", YouTubeUrlHelper.extractHandle("https://youtube.com/@411n"));
        assertEquals("spidex", YouTubeUrlHelper.extractHandle("@spidex"));
        assertEquals("c_artist", YouTubeUrlHelper.extractHandle("https://www.youtube.com/c/c_artist"));
        assertEquals("user_artist", YouTubeUrlHelper.extractHandle("https://www.youtube.com/user/user_artist"));

        assertNull(YouTubeUrlHelper.extractHandle("https://www.youtube.com/watch?v=kxpcV6650V4"));
        assertNull(YouTubeUrlHelper.extractHandle(""));
    }

    @Test
    void isYouTubeUrl_DetectsValidPatterns() {
        assertTrue(YouTubeUrlHelper.isYouTubeUrl("https://www.youtube.com/@411n"));
        assertTrue(YouTubeUrlHelper.isYouTubeUrl("https://youtu.be/5qap5aO4i9A"));
        assertTrue(YouTubeUrlHelper.isYouTubeUrl("@miragemfxnk"));
        assertFalse(YouTubeUrlHelper.isYouTubeUrl("https://open.spotify.com/artist/123"));
    }

    @Test
    void buildUrls_GeneratesCorrectLinks() {
        assertEquals("https://www.youtube.com/watch?v=abc123xyz00", YouTubeUrlHelper.buildWatchUrl("abc123xyz00"));
        assertEquals("https://www.youtube.com/embed/abc123xyz00", YouTubeUrlHelper.buildEmbedUrl("abc123xyz00"));
        assertEquals("https://i.ytimg.com/vi/abc123xyz00/hqdefault.jpg", YouTubeUrlHelper.buildThumbnailUrl("abc123xyz00"));
    }
}
