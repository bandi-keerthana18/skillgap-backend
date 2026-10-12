package com.skillgap.backend.service;

import com.skillgap.backend.dto.HistoryPoint;
import com.skillgap.backend.dto.LearnResult;
import com.skillgap.backend.dto.StudentSkillRequest;
import com.skillgap.backend.dto.WhatIfResult;
import com.skillgap.backend.entity.AssessmentHistory;
import com.skillgap.backend.entity.CareerRole;
import com.skillgap.backend.entity.RoleSkill;
import com.skillgap.backend.entity.Skill;
import com.skillgap.backend.entity.StudentSkill;
import com.skillgap.backend.entity.User;
import com.skillgap.backend.repository.AssessmentHistoryRepository;
import com.skillgap.backend.repository.CareerRoleRepository;
import com.skillgap.backend.repository.RoleSkillRepository;
import com.skillgap.backend.repository.SkillRepository;
import com.skillgap.backend.repository.StudentSkillRepository;
import com.skillgap.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final AnalysisService analysis;
    private final UserRepository userRepo;
    private final SkillRepository skillRepo;
    private final CareerRoleRepository roleRepo;
    private final RoleSkillRepository roleSkillRepo;
    private final StudentSkillRepository studentSkillRepo;
    private final AssessmentHistoryRepository historyRepo;

    // mark a skill as learned: set the new level and award 10 XP per level gained
    @Transactional
    public LearnResult learn(String email, StudentSkillRequest req) {
        if (req.skillId() == null || req.level() < 1 || req.level() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Give a skillId and a level from 1 to 5");
        }
        User u = analysis.user(email);
        Skill skill = skillRepo.findById(req.skillId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Unknown skill id " + req.skillId()));

        StudentSkill ss = null;
        for (StudentSkill x : studentSkillRepo.findByUserId(u.getId())) {
            if (x.getSkill().getId().equals(skill.getId())) {
                ss = x;
                break;
            }
        }
        int old = ss == null ? 0 : ss.getLevel();
        if (ss == null) {
            ss = new StudentSkill();
            ss.setUser(u);
            ss.setSkill(skill);
        }
        ss.setLevel(req.level());
        studentSkillRepo.save(ss);

        int gained = Math.max(0, req.level() - old) * 10;
        u.setXp(u.getXp() + gained);
        userRepo.save(u);

        return new LearnResult(skill.getId(), skill.getName(), req.level(), gained, u.getXp());
    }

    // save a snapshot of the current scores for a role
    @Transactional
    public HistoryPoint assess(String email, Long roleId) {
        User u = analysis.user(email);
        CareerRole role = roleRepo.findById(roleId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
        Map<Long, Integer> lv = analysis.levels(u.getId());
        List<RoleSkill> reqs = roleSkillRepo.findByCareerRoleId(roleId);

        AssessmentHistory h = new AssessmentHistory();
        h.setUser(u);
        h.setCareerRole(role);
        h.setReadiness(analysis.readiness(reqs, lv));
        h.setAlignment(analysis.alignment(reqs, lv));
        h.setTakenAt(LocalDateTime.now());
        historyRepo.save(h);
        return toPoint(h);
    }

    public List<HistoryPoint> history(String email, Long roleId) {
        User u = analysis.user(email);
        return historyRepo.findByUserIdAndCareerRoleIdOrderByTakenAtAsc(u.getId(), roleId)
                .stream().map(this::toPoint).toList();
    }

    // what-if: apply temporary skill changes and compare the scores, nothing is saved
    public WhatIfResult whatIf(String email, Long roleId, List<StudentSkillRequest> changes) {
        User u = analysis.user(email);
        CareerRole role = roleRepo.findById(roleId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
        Map<Long, Integer> before = analysis.levels(u.getId());
        Map<Long, Integer> after = new HashMap<>(before);

        for (StudentSkillRequest c : changes) {
            if (c.skillId() == null || c.level() < 0 || c.level() > 5) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Each change needs a skillId and a level from 0 to 5");
            }
            if (c.level() == 0) after.remove(c.skillId());
            else after.put(c.skillId(), c.level());
        }

        List<RoleSkill> reqs = roleSkillRepo.findByCareerRoleId(roleId);
        double rb = analysis.readiness(reqs, before);
        double ra = analysis.readiness(reqs, after);
        double ab = analysis.alignment(reqs, before);
        double aa = analysis.alignment(reqs, after);
        return new WhatIfResult(roleId, role.getTitle(), rb, ra, ab, aa,
                round1(ra - rb), round1(aa - ab));
    }

    private HistoryPoint toPoint(AssessmentHistory h) {
        return new HistoryPoint(h.getTakenAt().toString(), h.getReadiness(), h.getAlignment());
    }

    private double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
