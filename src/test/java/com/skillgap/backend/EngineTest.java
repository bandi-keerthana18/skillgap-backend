package com.skillgap.backend;

import com.skillgap.backend.entity.CareerRole;
import com.skillgap.backend.entity.RoleSkill;
import com.skillgap.backend.entity.Skill;
import com.skillgap.backend.repository.SkillRepository;
import com.skillgap.backend.service.AnalysisService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EngineTest {

    private Skill skill(long id, String name) {
        Skill s = new Skill();
        s.setId(id);
        s.setName(name);
        return s;
    }

    private RoleSkill req(Skill s, int level, double weight) {
        RoleSkill rs = new RoleSkill();
        rs.setCareerRole(new CareerRole());
        rs.setSkill(s);
        rs.setRequiredLevel(level);

        rs.setWeight(weight);
        return rs;
    }

    private AnalysisService engine(List<Skill> all) {
        SkillRepository repo = (SkillRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{SkillRepository.class},
                (proxy, method, args) -> method.getName().equals("findAll") ? all : null);
        return new AnalysisService(null, repo, null, null, null);
    }

    @Test
    void readinessMatchesHandCalculation() {
        Skill java = skill(1, "Java");
        Skill sql = skill(2, "SQL");
        Skill react = skill(3, "React");
        AnalysisService e = engine(List.of(java, sql, react));
        List<RoleSkill> reqs = List.of(req(java, 4, 0.9), req(sql, 3, 0.7), req(react, 4, 0.8));
        Map<Long, Integer> levels = Map.of(1L, 4, 2L, 3);
        // (0.9*1 + 0.7*1 + 0.8*0) / 2.4 * 100 = 66.7
        assertEquals(66.7, e.readiness(reqs, levels), 0.001);
    }

    @Test
    void alignmentMatchesHandCalculation() {
        Skill java = skill(1, "Java");
        Skill sql = skill(2, "SQL");
        Skill react = skill(3, "React");
        AnalysisService e = engine(List.of(java, sql, react));
        List<RoleSkill> reqs = List.of(req(java, 4, 0.9), req(sql, 3, 0.7), req(react, 4, 0.8));
        Map<Long, Integer> levels = Map.of(1L, 4, 2L, 3);

        // dot = 20.7, |student| = 5, |role| = 5.2546, cosine = 0.788 -> 78.8
        assertEquals(78.8, e.alignment(reqs, levels), 0.001);
    }

    @Test
    void studentWithNoSkillsScoresZero() {
        Skill java = skill(1, "Java");
        AnalysisService e = engine(List.of(java));
        List<RoleSkill> reqs = List.of(req(java, 4, 0.9));
        assertEquals(0.0, e.readiness(reqs, Map.of()), 0.001);
        assertEquals(0.0, e.alignment(reqs, Map.of()), 0.001);
    }

    @Test
    void studentMeetingEveryRequirementIsFullyReady() {
        Skill java = skill(1, "Java");
        Skill sql = skill(2, "SQL");
        AnalysisService e = engine(List.of(java, sql));
        List<RoleSkill> reqs = List.of(req(java, 4, 0.9), req(sql, 3, 0.7));
        assertEquals(100.0, e.readiness(reqs, Map.of(1L, 5, 2L, 3)), 0.001);
    }
}
