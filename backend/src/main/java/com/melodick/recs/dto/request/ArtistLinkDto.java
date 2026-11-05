package com.melodick.recs.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ArtistLinkDto {

    private Long id;

    @NotBlank(message = "Tên nền tảng không được để trống")
    @Size(max = 50, message = "Tên nền tảng tối đa 50 ký tự")
    private String platform;

    @Size(max = 1000, message = "URL tối đa 1000 ký tự")
    private String url;

    private int displayOrder = 1;

    public ArtistLinkDto() {}

    public ArtistLinkDto(String platform, String url, int displayOrder) {
        this.platform = platform;
        this.url = url;
        this.displayOrder = displayOrder;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
}
