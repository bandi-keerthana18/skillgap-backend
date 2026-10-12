package com.skillgap.backend.service;

import com.skillgap.backend.dto.AuthResponse;
import com.skillgap.backend.dto.LoginRequest;
import com.skillgap.backend.dto.RegisterRequest;
import com.skillgap.backend.entity.Role;
import com.skillgap.backend.entity.User;
import com.skillgap.backend.repository.UserRepository;
import com.skillgap.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest req) {
        if (userRepo.existsByEmail(req.email())) {

            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        User u = new User();
        u.setName(req.name());
        u.setEmail(req.email());
        u.setPassword(encoder.encode(req.password()));
        u.setRole(Role.STUDENT);
        userRepo.save(u);
        return toResponse(u);
    }

    public AuthResponse login(LoginRequest req) {
        User u = userRepo.findByEmail(req.email())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (!encoder.matches(req.password(), u.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        return toResponse(u);
    }

    private AuthResponse toResponse(User u) {
        String token = jwtService.generateToken(u.getEmail(), u.getRole().name());
        return new AuthResponse(token, u.getName(), u.getEmail(), u.getRole().name());
    }
}
