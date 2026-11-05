package com.melodick.recs.dto.response;

public class AuthResponse {
    private String token;
    private AdminUserResponse user;

    public AuthResponse() {}

    public AuthResponse(String token, AdminUserResponse user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public AdminUserResponse getUser() { return user; }
    public void setUser(AdminUserResponse user) { this.user = user; }
}

