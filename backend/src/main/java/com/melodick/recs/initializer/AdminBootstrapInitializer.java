package com.melodick.recs.initializer;

import com.melodick.recs.entity.AdminUser;
import com.melodick.recs.repository.AdminUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrapInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminBootstrapInitializer.class);

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.default-username:admin}")
    private String defaultUsername;

    @Value("${app.admin.default-password:Admin@123456}")
    private String defaultPassword;

    public AdminBootstrapInitializer(AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        adminUserRepository.findByUsername(defaultUsername).ifPresentOrElse(
                admin -> {
                    if (!passwordEncoder.matches(defaultPassword, admin.getPasswordHash())) {
                        admin.setPasswordHash(passwordEncoder.encode(defaultPassword));
                        adminUserRepository.save(admin);
                        logger.info("Admin user [{}] password successfully synced with default configuration.", defaultUsername);
                    } else {
                        logger.info("Admin user [{}] credentials verified and ready.", defaultUsername);
                    }
                },
                () -> {
                    AdminUser newAdmin = new AdminUser();
                    newAdmin.setUsername(defaultUsername);
                    newAdmin.setPasswordHash(passwordEncoder.encode(defaultPassword));
                    newAdmin.setDisplayName("Super Admin");
                    newAdmin.setActive(true);
                    adminUserRepository.save(newAdmin);
                    logger.info("Created initial admin user [{}] with BCrypt hashed credentials.", defaultUsername);
                }
        );
    }
}

