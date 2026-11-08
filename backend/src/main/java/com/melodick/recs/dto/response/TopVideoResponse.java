package com.melodick.recs.dto.response;

public class TopVideoResponse {
    private String videoId;
    private String title;
    private String url;
    private String thumbnailUrl;
    private Long viewCount;
    private String formattedViews;
    private String embedUrl;
    private String platform;

    public TopVideoResponse() {}

    public TopVideoResponse(String videoId, String title, String url, String thumbnailUrl,
                            Long viewCount, String formattedViews, String embedUrl, String platform) {
        this.videoId = videoId;
        this.title = title;
        this.url = url;
        this.thumbnailUrl = thumbnailUrl;
        this.viewCount = viewCount;
        this.formattedViews = formattedViews;
        this.embedUrl = embedUrl;
        this.platform = platform;
    }

    public String getVideoId() { return videoId; }
    public void setVideoId(String videoId) { this.videoId = videoId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public Long getViewCount() { return viewCount; }
    public void setViewCount(Long viewCount) { this.viewCount = viewCount; }

    public String getFormattedViews() { return formattedViews; }
    public void setFormattedViews(String formattedViews) { this.formattedViews = formattedViews; }

    public String getEmbedUrl() { return embedUrl; }
    public void setEmbedUrl(String embedUrl) { this.embedUrl = embedUrl; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
}
