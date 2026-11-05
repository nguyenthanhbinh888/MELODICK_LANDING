package com.melodick.recs.service;

import com.melodick.recs.dto.request.LoginRequest;
import com.melodick.recs.dto.response.AdminUserResponse;
import com.melodick.recs.dto.response.AuthResponse;
import com.melodick.recs.entity.AdminUser;
import com.melodick.recs.exception.UnauthorizedException;
import com.melodick.recs.repository.AdminUserRepository;
import com.melodick.recs.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(AdminUserRepository adminUserRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        AdminUser admin = adminUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Tên đăng nhập hoặc mật khẩu không chính xác"));

        if (!admin.isActive()) {
            throw new UnauthorizedException("Tài khoản quản trị viên này đã bị vô hiệu hóa");
        }

        if (!passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
            throw new UnauthorizedException("Tên đăng nhập hoặc mật khẩu không chính xác");
        }

        admin.setLastLoginAt(LocalDateTime.now());
        adminUserRepository.save(admin);

        String token = tokenProvider.generateToken(admin.getUsername());
        AdminUserResponse userResponse = new AdminUserResponse(
                admin.getId(),
                admin.getUsername(),
                admin.getDisplayName(),
                admin.isActive(),
                admin.getLastLoginAt()
        );

        return new AuthResponse(token, userResponse);
    }

    @Transactional(readOnly = true)
    public AdminUserResponse getCurrentAdmin(String username) {
        AdminUser admin = adminUserRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("Không tìm thấy thông tin quản trị viên"));

        return new AdminUserResponse(
                admin.getId(),
                admin.getUsername(),
                admin.getDisplayName(),
                admin.isActive(),
                admin.getLastLoginAt()
        );
    }
}
