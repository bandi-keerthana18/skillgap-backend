package com.skillgap.backend.controller;

import com.skillgap.backend.entity.User;
import com.skillgap.backend.repository.StudentSkillRepository;
import com.skillgap.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepo;
    private final StudentSkillRepository studentSkillRepo;

    @GetMapping("/profile")
    public Map<String, Object> profile(Authentication auth) {
        User u = userRepo.findByEmail(auth.getName()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        return Map.of(
                "name", u.getName(),
                "email", u.getEmail(),
                "role", u.getRole().name(),
                "xp", u.getXp(),
                "skillCount", studentSkillRepo.findByUserId(u.getId()).size());
    }
}
