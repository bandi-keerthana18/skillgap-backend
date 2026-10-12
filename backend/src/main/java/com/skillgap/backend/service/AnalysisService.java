package com.skillgap.backend.service;

import com.skillgap.backend.dto.*;
import com.skillgap.backend.entity.*;
import com.skillgap.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final UserRepository userRepo;
    private final SkillRepository skillRepo;
    private final CareerRoleRepository roleRepo;
    private final RoleSkillRepository roleSkillRepo;
    private final StudentSkillRepository studentSkillRepo;

    public User user(String email) {
        return userRepo.findByEmail(email).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    // ---------- student skills ----------

    public List<StudentSkillView> mySkills(String email) {

        return studentSkillRepo.findByUserId(user(email).getId()).stream()
                .map(ss -> new StudentSkillView(
                        ss.getSkill().getId(), ss.getSkill().getName(), ss.getLevel()))
                .toList();
    }

    @Transactional
    public List<StudentSkillView> saveSkills(String email, List<StudentSkillRequest> requests) {
        User u = user(email);
        Map<Long, StudentSkill> existing = new HashMap<>();
        for (StudentSkill ss : studentSkillRepo.findByUserId(u.getId())) {
            existing.put(ss.getSkill().getId(), ss);
        }

        for (StudentSkillRequest r : requests) {
            if (r.skillId() == null || r.level() < 0 || r.level() > 5) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Each skill needs a skillId and a level from 0 to 5");
            }
            StudentSkill ss = existing.get(r.skillId());
            if (r.level() == 0) {
                if (ss != null) {
                    studentSkillRepo.delete(ss);
                    existing.remove(r.skillId());
                }
                continue;
            }
            if (ss == null) {
                Skill skill = skillRepo.findById(r.skillId()).orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Unknown skill id " + r.skillId()));
                ss = new StudentSkill();

                ss.setUser(u);
                ss.setSkill(skill);
            }
            ss.setLevel(r.level());
            studentSkillRepo.save(ss);
        }
        return mySkills(email);
    }

    public Map<Long, Integer> levels(Long userId) {
        Map<Long, Integer> m = new HashMap<>();
        for (StudentSkill ss : studentSkillRepo.findByUserId(userId)) {
            m.put(ss.getSkill().getId(), ss.getLevel());
        }
        return m;
    }

    // ---------- gap analysis ----------

    public GapReport analyse(String email, Long roleId) {
        User u = user(email);
        CareerRole role = roleRepo.findById(roleId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
        Map<Long, Integer> lv = levels(u.getId());
        List<RoleSkill> reqs = roleSkillRepo.findByCareerRoleId(roleId);

        List<GapItem> missing = new ArrayList<>();
        List<GapItem> weak = new ArrayList<>();
        List<GapItem> strong = new ArrayList<>();

        for (RoleSkill rs : reqs) {
            int have = lv.getOrDefault(rs.getSkill().getId(), 0);

            int need = rs.getRequiredLevel();
            int gap = Math.max(0, need - have);
            String status = have == 0 ? "MISSING" : (have < need ? "WEAK" : "STRONG");
            GapItem item = new GapItem(rs.getSkill().getId(), rs.getSkill().getName(),
                    need, have, gap, rs.getWeight(), status);
            if (have == 0) missing.add(item);
            else if (have < need) weak.add(item);
            else strong.add(item);
        }

        Comparator<GapItem> byImpact =
                Comparator.comparingDouble((GapItem g) -> g.weight() * g.gap()).reversed();
        missing.sort(byImpact);
        weak.sort(byImpact);

        return new GapReport(roleId, role.getTitle(), readiness(reqs, lv),
                alignment(reqs, lv), missing, weak, strong);
    }

    // readiness = sum(weight * min(have, need) / need) / sum(weight) * 100
    public double readiness(List<RoleSkill> reqs, Map<Long, Integer> lv) {
        double num = 0, den = 0;
        for (RoleSkill rs : reqs) {
            int need = rs.getRequiredLevel();
            if (need <= 0) continue;
            int have = lv.getOrDefault(rs.getSkill().getId(), 0);
            num += rs.getWeight() * Math.min(have, need) / (double) need;
            den += rs.getWeight();
        }
        return den == 0 ? 0 : round1(num / den * 100);
    }


    // cosine similarity between student vector and role vector over all skills
    // role vector value = requiredLevel * weight, student vector value = level
    public double alignment(List<RoleSkill> reqs, Map<Long, Integer> lv) {
        Map<Long, Double> role = new HashMap<>();
        for (RoleSkill rs : reqs) {
            role.put(rs.getSkill().getId(), rs.getRequiredLevel() * rs.getWeight());
        }
        double dot = 0, normS = 0, normR = 0;
        for (Skill s : skillRepo.findAll()) {
            double a = lv.getOrDefault(s.getId(), 0);
            double b = role.getOrDefault(s.getId(), 0.0);
            dot += a * b;
            normS += a * a;
            normR += b * b;
        }
        if (normS == 0 || normR == 0) return 0;
        return round1(dot / (Math.sqrt(normS) * Math.sqrt(normR)) * 100);
    }

    // ---------- career ranking ----------

    public List<RankedRole> ranking(String email) {
        Map<Long, Integer> lv = levels(user(email).getId());
        List<RankedRole> out = new ArrayList<>();
        for (CareerRole r : roleRepo.findAll()) {
            List<RoleSkill> reqs = roleSkillRepo.findByCareerRoleId(r.getId());
            out.add(new RankedRole(r.getId(), r.getTitle(),
                    alignment(reqs, lv), readiness(reqs, lv)));
        }
        out.sort(Comparator.comparingDouble(RankedRole::alignmentPercent).reversed());
        return out;
    }


    private double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
