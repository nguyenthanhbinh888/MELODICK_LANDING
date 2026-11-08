package com.melodick.recs.dto.response;

import java.time.LocalDateTime;

public class ArtistStatsResponse {
    private Long artistId;
    private String artistName;
    private String platform;
    private String channelTitle;
    private Long totalViews;
    private String formattedViews;
    private Long topVideoViews;
    private String formattedTopVideoViews;
    private Long followersCount;
    private String formattedFollowers;
    private Integer videoCount;
    private TopVideoResponse topVideo;
    private String source;
    private LocalDateTime lastFetchedAt;

    public ArtistStatsResponse() {}

    public Long getArtistId() { return artistId; }
    public void setArtistId(Long artistId) { this.artistId = artistId; }

    public String getArtistName() { return artistName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getChannelTitle() { return channelTitle; }
    public void setChannelTitle(String channelTitle) { this.channelTitle = channelTitle; }

    public Long getTotalViews() { return totalViews; }
    public void setTotalViews(Long totalViews) { this.totalViews = totalViews; }

    public String getFormattedViews() { return formattedViews; }
    public void setFormattedViews(String formattedViews) { this.formattedViews = formattedViews; }

    public Long getTopVideoViews() { return topVideoViews; }
    public void setTopVideoViews(Long topVideoViews) { this.topVideoViews = topVideoViews; }

    public String getFormattedTopVideoViews() { return formattedTopVideoViews; }
    public void setFormattedTopVideoViews(String formattedTopVideoViews) { this.formattedTopVideoViews = formattedTopVideoViews; }

    public Long getFollowersCount() { return followersCount; }
    public void setFollowersCount(Long followersCount) { this.followersCount = followersCount; }

    public String getFormattedFollowers() { return formattedFollowers; }
    public void setFormattedFollowers(String formattedFollowers) { this.formattedFollowers = formattedFollowers; }

    public Integer getVideoCount() { return videoCount; }
    public void setVideoCount(Integer videoCount) { this.videoCount = videoCount; }

    public TopVideoResponse getTopVideo() { return topVideo; }
    public void setTopVideo(TopVideoResponse topVideo) { this.topVideo = topVideo; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public LocalDateTime getLastFetchedAt() { return lastFetchedAt; }
    public void setLastFetchedAt(LocalDateTime lastFetchedAt) { this.lastFetchedAt = lastFetchedAt; }

    /**
     * Utility helper to format counts into compact strings: e.g. 1,250,000 -> "1.25M", 2,500,000,000 -> "2.5B"
     * Handles boundary rounding thresholds (e.g. 999,950 -> "1M", 999,950,000 -> "1B") to prevent "1000K" / "1000M".
     */
    public static String formatMetricCount(Long count) {
        if (count == null || count <= 0) return "0";
        if (count >= 1_000_000_000L) {
            String val = String.format(java.util.Locale.US, "%.2f", count / 1_000_000_000.0);
            return cleanTrailingZeros(val) + "B";
        }
        if (count >= 999_950_000L) {
            return "1B";
        }
        if (count >= 1_000_000L) {
            String val = String.format(java.util.Locale.US, "%.2f", count / 1_000_000.0);
            return cleanTrailingZeros(val) + "M";
        }
        if (count >= 999_950L) {
            return "1M";
        }
        if (count >= 1_000L) {
            String val = String.format(java.util.Locale.US, "%.1f", count / 1_000.0);
            return cleanTrailingZeros(val) + "K";
        }
        return String.valueOf(count);
    }

    private static String cleanTrailingZeros(String val) {
        if (val.contains(".")) {
            val = val.replaceAll("0+$", "").replaceAll("\\.$", "");
        }
        return val;
    }
}
