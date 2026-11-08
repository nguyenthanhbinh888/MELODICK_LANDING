package com.melodick.recs.service.platform;

public class PlatformFetchResult {
    private String platform = "YouTube";
    private String channelTitle;
    private Long totalViews = 0L;
    private Long followersCount = 0L;
    private Integer videoCount = 0;
    private String topVideoId;
    private String topVideoTitle;
    private String topVideoUrl;
    private String topVideoThumbnail;
    private Long topVideoViews = 0L;
    private String topVideoEmbedUrl;
    private String source = "FALLBACK";
    private boolean success = false;

    public PlatformFetchResult() {}

    public static PlatformFetchResult failed(String platform, String source) {
        PlatformFetchResult result = new PlatformFetchResult();
        result.setPlatform(platform);
        result.setSource(source);
        result.setSuccess(false);
        return result;
    }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getChannelTitle() { return channelTitle; }
    public void setChannelTitle(String channelTitle) { this.channelTitle = channelTitle; }

    public Long getTotalViews() { return totalViews != null ? totalViews : 0L; }
    public void setTotalViews(Long totalViews) { this.totalViews = totalViews; }

    public Long getFollowersCount() { return followersCount != null ? followersCount : 0L; }
    public void setFollowersCount(Long followersCount) { this.followersCount = followersCount; }

    public Integer getVideoCount() { return videoCount != null ? videoCount : 0; }
    public void setVideoCount(Integer videoCount) { this.videoCount = videoCount; }

    public String getTopVideoId() { return topVideoId; }
    public void setTopVideoId(String topVideoId) { this.topVideoId = topVideoId; }

    public String getTopVideoTitle() { return topVideoTitle; }
    public void setTopVideoTitle(String topVideoTitle) { this.topVideoTitle = topVideoTitle; }

    public String getTopVideoUrl() { return topVideoUrl; }
    public void setTopVideoUrl(String topVideoUrl) { this.topVideoUrl = topVideoUrl; }

    public String getTopVideoThumbnail() { return topVideoThumbnail; }
    public void setTopVideoThumbnail(String topVideoThumbnail) { this.topVideoThumbnail = topVideoThumbnail; }

    public Long getTopVideoViews() { return topVideoViews != null ? topVideoViews : 0L; }
    public void setTopVideoViews(Long topVideoViews) { this.topVideoViews = topVideoViews; }

    public String getTopVideoEmbedUrl() { return topVideoEmbedUrl; }
    public void setTopVideoEmbedUrl(String topVideoEmbedUrl) { this.topVideoEmbedUrl = topVideoEmbedUrl; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}
