package com.melodick.recs.dto.response;

import java.time.LocalDateTime;

public class AdminUserResponse {
    private Long id;
    private String username;
    private String displayName;
    private boolean active;
    private LocalDateTime lastLoginAt;

    public AdminUserResponse() {}

    public AdminUserResponse(Long id, String username, String displayName, boolean active, LocalDateTime lastLoginAt) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.active = active;
        this.lastLoginAt = lastLoginAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
}

