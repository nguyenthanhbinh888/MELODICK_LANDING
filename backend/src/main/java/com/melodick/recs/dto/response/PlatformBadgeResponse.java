package com.melodick.recs.dto.response;

public class PlatformBadgeResponse {
    private String platform;
    private String metricType;
    private Long value;
    private String formattedValue;
    private String label;
    private String formattedBadge;
    private String tooltip;
    private String url;
    private String icon;

    public PlatformBadgeResponse() {}

    public PlatformBadgeResponse(String platform, String metricType, Long value,
                                 String formattedValue, String label, String formattedBadge,
                                 String tooltip, String url, String icon) {
        this.platform = platform;
        this.metricType = metricType;
        this.value = value;
        this.formattedValue = formattedValue;
        this.label = label;
        this.formattedBadge = formattedBadge;
        this.tooltip = tooltip;
        this.url = url;
        this.icon = icon;
    }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getMetricType() { return metricType; }
    public void setMetricType(String metricType) { this.metricType = metricType; }

    public Long getValue() { return value; }
    public void setValue(Long value) { this.value = value; }

    public String getFormattedValue() { return formattedValue; }
    public void setFormattedValue(String formattedValue) { this.formattedValue = formattedValue; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getFormattedBadge() { return formattedBadge; }
    public void setFormattedBadge(String formattedBadge) { this.formattedBadge = formattedBadge; }

    public String getTooltip() { return tooltip; }
    public void setTooltip(String tooltip) { this.tooltip = tooltip; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
}
