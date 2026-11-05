package com.melodick.recs.controller;

import com.melodick.recs.dto.request.LoginRequest;
import com.melodick.recs.dto.response.AdminUserResponse;
import com.melodick.recs.dto.response.ApiResponse;
import com.melodick.recs.dto.response.AuthResponse;
import com.melodick.recs.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.ok("Đã đăng xuất thành công"));
    }

    @GetMapping("/me")
    public ResponseEntity<AdminUserResponse> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(authService.getCurrentAdmin(userDetails.getUsername()));
    }
}
