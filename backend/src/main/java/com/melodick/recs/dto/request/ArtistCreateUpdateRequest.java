package com.melodick.recs.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class ArtistCreateUpdateRequest {

    @NotBlank(message = "Tên nghệ sĩ không được để trống")
    @Size(max = 150, message = "Tên nghệ sĩ tối đa 150 ký tự")
    private String name;

    @Size(max = 180, message = "Slug tối đa 180 ký tự")
    private String slug;

    @Size(max = 1000, message = "URL ảnh đại diện tối đa 1000 ký tự")
    private String imageUrl;

    private String bio;

    private boolean published = true;

    @Min(value = 1, message = "Thứ tự hiển thị phải lớn hơn hoặc bằng 1")
    private int displayOrder = 1;

    @Valid
    private List<ArtistLinkDto> links = new ArrayList<>();

    public ArtistCreateUpdateRequest() {}

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

    public List<ArtistLinkDto> getLinks() { return links; }
    public void setLinks(List<ArtistLinkDto> links) { this.links = links; }
}

