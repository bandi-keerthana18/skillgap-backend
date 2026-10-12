package com.skillgap.backend.controller;

import com.skillgap.backend.dto.*;
import com.skillgap.backend.repository.CareerRoleRepository;
import com.skillgap.backend.repository.SkillRepository;
import com.skillgap.backend.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService service;
    private final SkillRepository skillRepo;
    private final CareerRoleRepository roleRepo;

    @GetMapping("/skills")
    public List<SkillDto> skills() {
        return skillRepo.findAll().stream()
                .map(s -> new SkillDto(s.getId(), s.getName(), s.getCategory()))
                .toList();
    }

    @GetMapping("/roles")
    public List<RoleDto> roles() {
        return roleRepo.findAll().stream()
                .map(r -> new RoleDto(r.getId(), r.getTitle(), r.getDescription()))
                .toList();
    }

    @GetMapping("/me/skills")
    public List<StudentSkillView> mySkills(Authentication auth) {
        return service.mySkills(auth.getName());
    }

    @PutMapping("/me/skills")
    public List<StudentSkillView> saveSkills(Authentication auth,
                                             @RequestBody List<StudentSkillRequest> body) {
        return service.saveSkills(auth.getName(), body);
    }

    @GetMapping("/analysis/ranking")
    public List<RankedRole> ranking(Authentication auth) {
        return service.ranking(auth.getName());
    }

    @GetMapping("/analysis/{roleId}")
    public GapReport analyse(Authentication auth, @PathVariable Long roleId) {
        return service.analyse(auth.getName(), roleId);
    }
}
