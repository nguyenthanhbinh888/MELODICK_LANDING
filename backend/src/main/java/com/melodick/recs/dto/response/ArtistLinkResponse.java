package com.melodick.recs.dto.response;

public class ArtistLinkResponse {
    private Long id;
    private String platform;
    private String url;
    private int displayOrder;

    public ArtistLinkResponse() {}

    public ArtistLinkResponse(Long id, String platform, String url, int displayOrder) {
        this.id = id;
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
