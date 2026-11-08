package com.melodick.recs.service.platform;

import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YouTubeUrlHelper {

    // Regex for video ID
    private static final Pattern VIDEO_ID_PATTERN = Pattern.compile(
            "(?:youtube\\.com\\/(?:watch\\?v=|embed\\/|v\\/|shorts\\/)|youtu\\.be\\/)([a-zA-Z0-9_-]{11})"
    );

    // Regex for channel ID (starts with UC and followed by 20-24 chars)
    private static final Pattern CHANNEL_ID_PATTERN = Pattern.compile(
            "youtube\\.com\\/(?:channel\\/)(UC[a-zA-Z0-9_-]{20,24})"
    );

    // Regex for handle (starts with @)
    private static final Pattern HANDLE_PATTERN = Pattern.compile(
            "youtube\\.com\\/@([a-zA-Z0-9_.-]+)"
    );

    // Regex for direct @handle string
    private static final Pattern DIRECT_HANDLE_PATTERN = Pattern.compile(
            "^@([a-zA-Z0-9_.-]+)$"
    );

    public static String extractVideoId(String input) {
        if (!StringUtils.hasText(input)) return null;
        String trimmed = input.trim();
        Matcher matcher = VIDEO_ID_PATTERN.matcher(trimmed);
        if (matcher.find()) {
            return matcher.group(1);
        }
        // If 11 chars alphanumeric and no slashes, could be direct video ID
        if (trimmed.matches("^[a-zA-Z0-9_-]{11}$")) {
            return trimmed;
        }
        return null;
    }

    public static String extractChannelId(String input) {
        if (!StringUtils.hasText(input)) return null;
        String trimmed = input.trim();
        Matcher matcher = CHANNEL_ID_PATTERN.matcher(trimmed);
        if (matcher.find()) {
            return matcher.group(1);
        }
        if (trimmed.matches("^UC[a-zA-Z0-9_-]{20,24}$")) {
            return trimmed;
        }
        return null;
    }

    public static String extractHandle(String input) {
        if (!StringUtils.hasText(input)) return null;
        String trimmed = input.trim();
        Matcher matcher = HANDLE_PATTERN.matcher(trimmed);
        if (matcher.find()) {
            return matcher.group(1);
        }
        Matcher directMatcher = DIRECT_HANDLE_PATTERN.matcher(trimmed);
        if (directMatcher.find()) {
            return directMatcher.group(1);
        }
        return null;
    }

    public static boolean isYouTubeUrl(String input) {
        if (!StringUtils.hasText(input)) return false;
        String lower = input.toLowerCase();
        return lower.contains("youtube.com") || lower.contains("youtu.be") || lower.startsWith("@");
    }

    public static String buildWatchUrl(String videoId) {
        if (!StringUtils.hasText(videoId)) return "";
        return "https://www.youtube.com/watch?v=" + videoId;
    }

    public static String buildEmbedUrl(String videoId) {
        if (!StringUtils.hasText(videoId)) return "";
        return "https://www.youtube.com/embed/" + videoId;
    }

    public static String buildThumbnailUrl(String videoId) {
        if (!StringUtils.hasText(videoId)) return "";
        return "https://i.ytimg.com/vi/" + videoId + "/hqdefault.jpg";
    }
}
