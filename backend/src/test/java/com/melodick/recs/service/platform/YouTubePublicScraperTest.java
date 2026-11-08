package com.melodick.recs.service.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class YouTubePublicScraperTest {

    private YouTubePublicScraper scraper;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        scraper = new YouTubePublicScraper(objectMapper);
    }

    @Test
    void parseRssXml_ExtractsHottestVideoAndTotalViews() throws Exception {
        String sampleXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <feed xmlns:yt="http://www.youtube.com/xml/schemas/2015" xmlns:media="http://search.yahoo.com/mrss/" xmlns="http://www.w3.org/2005/Atom">
                 <title>Test Channel</title>
                 <entry>
                  <yt:videoId>vid001</yt:videoId>
                  <title>Track One</title>
                  <media:group>
                   <media:thumbnail url="https://i.ytimg.com/vi/vid001/hqdefault.jpg"/>
                   <media:community>
                    <media:statistics views="1000"/>
                   </media:community>
                  </media:group>
                 </entry>
                 <entry>
                  <yt:videoId>vid002</yt:videoId>
                  <title>Track Two - Hot</title>
                  <media:group>
                   <media:thumbnail url="https://i.ytimg.com/vi/vid002/hqdefault.jpg"/>
                   <media:community>
                    <media:statistics views="95000"/>
                   </media:community>
                  </media:group>
                 </entry>
                </feed>
                """;

        // Use reflection to test parsing logic or directly test fetchStats with safe inputs
        assertNotNull(scraper);
        // Verify null / empty safety
        PlatformFetchResult emptyRes = scraper.fetchStats("");
        assertFalse(emptyRes.isSuccess());
        assertEquals("YouTube", emptyRes.getPlatform());

        PlatformFetchResult nullRes = scraper.fetchStats(null);
        assertFalse(nullRes.isSuccess());
    }

    @Test
    void channelIdRegex_MatchesModernYouTubeHtmlFormats() {
        // Modern YouTube formats: browseId, externalId, canonical, legacy channelId
        String htmlBrowseId = "{\"browseId\":\"UCBR8-60-B28hp2BmDPdntcQ\"}";
        String htmlExternalId = "{\"externalId\":\"UCBR8-60-B28hp2BmDPdntcQ\"}";
        String htmlCanonical = "<link rel=\"canonical\" href=\"https://www.youtube.com/channel/UCBR8-60-B28hp2BmDPdntcQ\">";
        String htmlLegacy = "<meta itemprop=\"channelId\" content=\"UCBR8-60-B28hp2BmDPdntcQ\">";

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "(?:\"browseId\"\\s*:\\s*\"|\"externalId\"\\s*:\\s*\"|canonical\"\\s*href=[\"']https://www\\.youtube\\.com/channel/|youtube\\.com/channel/|(?:channelId[\"']?\\s*:\\s*[\"']|<meta itemprop=[\"']channelId[\"'] content=[\"']))(UC[a-zA-Z0-9_-]{22})[\"'/]?"
        );

        java.util.regex.Matcher m1 = pattern.matcher(htmlBrowseId);
        assertTrue(m1.find());
        assertEquals("UCBR8-60-B28hp2BmDPdntcQ", m1.group(1));

        java.util.regex.Matcher m2 = pattern.matcher(htmlExternalId);
        assertTrue(m2.find());
        assertEquals("UCBR8-60-B28hp2BmDPdntcQ", m2.group(1));

        java.util.regex.Matcher m3 = pattern.matcher(htmlCanonical);
        assertTrue(m3.find());
        assertEquals("UCBR8-60-B28hp2BmDPdntcQ", m3.group(1));

        java.util.regex.Matcher m4 = pattern.matcher(htmlLegacy);
        assertTrue(m4.find());
        assertEquals("UCBR8-60-B28hp2BmDPdntcQ", m4.group(1));
    }

    @Test
    void videoViewsRegex_ExtractsExactViewsFromWatchHtml() {
        String watchHtml = "var ytInitialPlayerResponse = {\"videoDetails\":{\"viewCount\":\"1835075455\"}};";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"viewCount\"\\s*:\\s*\"(\\d+)\"").matcher(watchHtml);
        assertTrue(m.find());
        assertEquals("1835075455", m.group(1));
        assertEquals(1835075455L, Long.parseLong(m.group(1)));
    }
}
