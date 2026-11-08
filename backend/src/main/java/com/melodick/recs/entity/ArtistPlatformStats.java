package com.melodick.recs.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "artist_platform_stats")
public class ArtistPlatformStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", nullable = false, unique = true)
    private Artist artist;

    @Column(nullable = false, length = 50)
    private String platform = "YouTube";

    @Column(name = "channel_title")
    private String channelTitle;

    @Column(name = "total_views", nullable = false)
    private Long totalViews = 0L;

    @Column(name = "followers_count", nullable = false)
    private Long followersCount = 0L;

    @Column(name = "video_count", nullable = false)
    private Integer videoCount = 0;

    @Column(name = "top_video_id", length = 100)
    private String topVideoId;

    @Column(name = "top_video_title")
    private String topVideoTitle;

    @Column(name = "top_video_url", length = 1000)
    private String topVideoUrl;

    @Column(name = "top_video_thumbnail", length = 1000)
    private String topVideoThumbnail;

    @Column(name = "top_video_views", nullable = false)
    private Long topVideoViews = 0L;

    @Column(name = "top_video_embed_url", length = 1000)
    private String topVideoEmbedUrl;

    @Column(nullable = false, length = 50)
    private String source = "SCRAPED";

    @Column(name = "last_fetched_at", nullable = false)
    private LocalDateTime lastFetchedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.lastFetchedAt == null) {
            this.lastFetchedAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public ArtistPlatformStats() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Artist getArtist() { return artist; }
    public void setArtist(Artist artist) { this.artist = artist; }

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

    public LocalDateTime getLastFetchedAt() { return lastFetchedAt; }
    public void setLastFetchedAt(LocalDateTime lastFetchedAt) { this.lastFetchedAt = lastFetchedAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
