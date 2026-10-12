package com.skillgap.backend.service;

import com.skillgap.backend.dto.ResourceDto;
import com.skillgap.backend.dto.Roadmap;
import com.skillgap.backend.dto.RoadmapStep;
import com.skillgap.backend.entity.CareerRole;
import com.skillgap.backend.entity.RoleSkill;
import com.skillgap.backend.entity.Skill;
import com.skillgap.backend.entity.User;
import com.skillgap.backend.repository.CareerRoleRepository;
import com.skillgap.backend.repository.ResourceRepository;
import com.skillgap.backend.repository.RoleSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RoadmapService {

    private final AnalysisService analysis;
    private final CareerRoleRepository roleRepo;
    private final RoleSkillRepository roleSkillRepo;
    private final ResourceRepository resourceRepo;

    @Transactional(readOnly = true)
    public Roadmap build(String email, Long roleId) {
        User u = analysis.user(email);
        CareerRole role = roleRepo.findById(roleId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
        Map<Long, Integer> lv = analysis.levels(u.getId());
        List<RoleSkill> reqs = roleSkillRepo.findByCareerRoleId(roleId);

        // skills the student still has to work on
        Map<Long, RoleSkill> todo = new LinkedHashMap<>();
        for (RoleSkill rs : reqs) {
            int have = lv.getOrDefault(rs.getSkill().getId(), 0);
            if (have < rs.getRequiredLevel()) {
                todo.put(rs.getSkill().getId(), rs);
            }
        }

        // repeatedly pick the most important skill whose prerequisites are done
        List<RoadmapStep> steps = new ArrayList<>();
        Set<Long> placed = new HashSet<>();
        while (placed.size() < todo.size()) {
            RoleSkill best = null;
            RoleSkill fallback = null;
            double bestScore = -1;

            for (RoleSkill rs : todo.values()) {
                Long sid = rs.getSkill().getId();
                if (placed.contains(sid)) continue;
                if (fallback == null) fallback = rs;

                boolean ready = true;
                for (Skill p : rs.getSkill().getPrerequisites()) {
                    if (todo.containsKey(p.getId()) && !placed.contains(p.getId())) {
                        ready = false;
                        break;
                    }
                }
                if (!ready) continue;

                double s = impact(rs, lv);
                if (s > bestScore) {
                    best = rs;
                    bestScore = s;
                }
            }
            if (best == null) best = fallback; // safety net if skills depend on each other in a loop

            Long sid = best.getSkill().getId();
            int have = lv.getOrDefault(sid, 0);
            placed.add(sid);

            List<String> prereqs = best.getSkill().getPrerequisites().stream()
                    .map(Skill::getName).sorted().toList();
            List<ResourceDto> res = resourceRepo.findBySkillId(sid).stream()
                    .map(r -> new ResourceDto(r.getTitle(), r.getUrl(), r.getType()))
                    .toList();

            steps.add(new RoadmapStep(steps.size() + 1, sid, best.getSkill().getName(),
                    have == 0 ? "MISSING" : "WEAK", have, best.getRequiredLevel(),
                    Math.round(impact(best, lv) * 100.0) / 100.0, prereqs, res));
        }

        return new Roadmap(roleId, role.getTitle(),
                analysis.readiness(reqs, lv), steps.size(), steps);
    }

    // importance of a skill = weight * gap
    private double impact(RoleSkill rs, Map<Long, Integer> lv) {
        int have = lv.getOrDefault(rs.getSkill().getId(), 0);
        return rs.getWeight() * (rs.getRequiredLevel() - have);
    }
}
