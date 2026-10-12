package com.skillgap.backend.controller;

import com.skillgap.backend.dto.*;
import com.skillgap.backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService admin;

    // skills
    @GetMapping("/skills")
    public List<AdminSkillView> skills() {
        return admin.skills();
    }

    @PostMapping("/skills")
    public AdminSkillView createSkill(@RequestBody SkillForm body) {
        return admin.createSkill(body);
    }

    @PutMapping("/skills/{id}")
    public AdminSkillView updateSkill(@PathVariable Long id, @RequestBody SkillForm body) {
        return admin.updateSkill(id, body);
    }

    @DeleteMapping("/skills/{id}")
    public Map<String, String> deleteSkill(@PathVariable Long id) {
        admin.deleteSkill(id);
        return Map.of("message", "Skill deleted");
    }

    // roles
    @PostMapping("/roles")
    public RoleDto createRole(@RequestBody RoleForm body) {
        return admin.createRole(body);
    }

    @PutMapping("/roles/{id}")
    public RoleDto updateRole(@PathVariable Long id, @RequestBody RoleForm body) {
        return admin.updateRole(id, body);
    }

    @DeleteMapping("/roles/{id}")
    public Map<String, String> deleteRole(@PathVariable Long id) {
        admin.deleteRole(id);
        return Map.of("message", "Role deleted");
    }

    // role requirements
    @GetMapping("/roles/{id}/skills")
    public List<RoleSkillView> roleSkills(@PathVariable Long id) {
        return admin.roleSkills(id);
    }

    @PutMapping("/roles/{id}/skills")
    public List<RoleSkillView> setRoleSkills(@PathVariable Long id,
                                             @RequestBody List<RoleSkillForm> body) {
        return admin.setRoleSkills(id, body);
    }

    // learning resources
    @GetMapping("/resources")
    public List<AdminResourceView> resources(@RequestParam(required = false) Long skillId) {
        return admin.resources(skillId);
    }

    @PostMapping("/resources")
    public AdminResourceView addResource(@RequestBody ResourceForm body) {
        return admin.addResource(body);
    }

    @DeleteMapping("/resources/{id}")
    public Map<String, String> deleteResource(@PathVariable Long id) {
        admin.deleteResource(id);
        return Map.of("message", "Resource deleted");
    }

    // analytics
    @GetMapping("/analytics")
    public Analytics analytics() {
        return admin.analytics();
    }
}
