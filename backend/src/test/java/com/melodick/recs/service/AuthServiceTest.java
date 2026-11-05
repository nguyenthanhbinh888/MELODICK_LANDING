package com.melodick.recs.service;

import com.melodick.recs.dto.request.LoginRequest;
import com.melodick.recs.dto.response.AuthResponse;
import com.melodick.recs.entity.AdminUser;
import com.melodick.recs.exception.UnauthorizedException;
import com.melodick.recs.repository.AdminUserRepository;
import com.melodick.recs.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AdminUserRepository adminUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private AdminUser testAdmin;

    @BeforeEach
    void setUp() {
        testAdmin = new AdminUser();
        testAdmin.setId(1L);
        testAdmin.setUsername("admin");
        testAdmin.setPasswordHash("$2a$10$hashedpassword");
        testAdmin.setDisplayName("Test Admin");
        testAdmin.setActive(true);
    }

    @Test
    void login_Successful_ReturnsTokenAndUser() {
        LoginRequest req = new LoginRequest("admin", "Admin@123456");

        when(adminUserRepository.findByUsername("admin")).thenReturn(Optional.of(testAdmin));
        when(passwordEncoder.matches("Admin@123456", testAdmin.getPasswordHash())).thenReturn(true);
        when(tokenProvider.generateToken("admin")).thenReturn("mock-jwt-token");

        AuthResponse response = authService.login(req);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("admin", response.getUser().getUsername());
        verify(adminUserRepository).save(testAdmin);
    }

    @Test
    void login_InvalidPassword_ThrowsUnauthorizedException() {
        LoginRequest req = new LoginRequest("admin", "wrong-password");

        when(adminUserRepository.findByUsername("admin")).thenReturn(Optional.of(testAdmin));
        when(passwordEncoder.matches("wrong-password", testAdmin.getPasswordHash())).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(req));
        verify(adminUserRepository, never()).save(any());
    }

    @Test
    void login_InactiveUser_ThrowsUnauthorizedException() {
        testAdmin.setActive(false);
        LoginRequest req = new LoginRequest("admin", "Admin@123456");

        when(adminUserRepository.findByUsername("admin")).thenReturn(Optional.of(testAdmin));

        assertThrows(UnauthorizedException.class, () -> authService.login(req));
        verify(adminUserRepository, never()).save(any());
    }
}

