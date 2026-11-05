package com.melodick.recs.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ArtistResponse {
    private Long id;
    private String name;
    private String slug;
    private String imageUrl;
    private String bio;
    private boolean published;
    private int displayOrder;
    private List<ArtistLinkResponse> links = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ArtistResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }

    public List<ArtistLinkResponse> getLinks() { return links; }
    public void setLinks(List<ArtistLinkResponse> links) { this.links = links; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
