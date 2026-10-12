package com.skillgap.backend.config;

import com.skillgap.backend.entity.Role;
import com.skillgap.backend.entity.User;
import com.skillgap.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        if (!userRepo.existsByEmail("admin@skillgap.com")) {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail("admin@skillgap.com");
            admin.setPassword(encoder.encode("Admin@123"));
            admin.setRole(Role.ADMIN);
            userRepo.save(admin);
        }
    }
}
