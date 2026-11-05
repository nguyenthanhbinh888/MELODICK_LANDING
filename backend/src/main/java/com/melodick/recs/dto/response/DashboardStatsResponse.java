package com.melodick.recs.dto.response;

public class DashboardStatsResponse {
    private long totalArtists;
    private long publishedArtists;
    private long hiddenArtists;

    public DashboardStatsResponse() {}

    public DashboardStatsResponse(long totalArtists, long publishedArtists, long hiddenArtists) {
        this.totalArtists = totalArtists;
        this.publishedArtists = publishedArtists;
        this.hiddenArtists = hiddenArtists;
    }

    public long getTotalArtists() { return totalArtists; }
    public void setTotalArtists(long totalArtists) { this.totalArtists = totalArtists; }

    public long getPublishedArtists() { return publishedArtists; }
    public void setPublishedArtists(long publishedArtists) { this.publishedArtists = publishedArtists; }

    public long getHiddenArtists() { return hiddenArtists; }
    public void setHiddenArtists(long hiddenArtists) { this.hiddenArtists = hiddenArtists; }
}
