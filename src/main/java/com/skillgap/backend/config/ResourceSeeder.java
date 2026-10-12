package com.skillgap.backend.config;

import com.skillgap.backend.entity.Resource;
import com.skillgap.backend.entity.Skill;
import com.skillgap.backend.repository.ResourceRepository;
import com.skillgap.backend.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

// Adds two starter learning links per skill (search links).
// The admin panel can replace them with hand-picked courses later.
@Component
@RequiredArgsConstructor
public class ResourceSeeder implements CommandLineRunner {

    private final SkillRepository skillRepo;
    private final ResourceRepository resourceRepo;

    @Override
    @Transactional
    public void run(String... args) {
        if (resourceRepo.count() > 0) {
            return;
        }
        for (Skill s : skillRepo.findAll()) {
            String q = URLEncoder.encode(s.getName(), StandardCharsets.UTF_8);
            resourceRepo.save(make(s, "Video tutorials: " + s.getName(),
                    "https://www.youtube.com/results?search_query=" + q + "+tutorial+for+beginners",
                    "VIDEO"));
            resourceRepo.save(make(s, "Articles and practice: " + s.getName(),
                    "https://www.google.com/search?q=learn+" + q, "ARTICLE"));
        }
    }

    private Resource make(Skill s, String title, String url, String type) {
        Resource r = new Resource();

        r.setSkill(s);
        r.setTitle(title);
        r.setUrl(url);
        r.setType(type);
        return r;
    }
}
