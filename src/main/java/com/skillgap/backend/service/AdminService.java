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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final SkillRepository skillRepo;
    private final CareerRoleRepository roleRepo;
    private final RoleSkillRepository roleSkillRepo;
    private final StudentSkillRepository studentSkillRepo;
    private final ResourceRepository resourceRepo;
    private final AssessmentHistoryRepository historyRepo;
    private final UserRepository userRepo;
    private final AnalysisService analysis;

    // ---------- skills ----------

    @Transactional(readOnly = true)
    public List<AdminSkillView> skills() {
        return skillRepo.findAll().stream().map(this::skillView).toList();
    }

    @Transactional
    public AdminSkillView createSkill(SkillForm f) {
        String name = clean(f.name(), "Skill name");
        if (skillRepo.findByName(name).isPresent()) {
            throw conflict("A skill with this name already exists");
        }
        Skill s = new Skill();
        s.setName(name);
        s.setCategory(f.category() == null ? null : f.category().trim());
        setPrerequisites(s, f.prerequisiteIds());
        return skillView(skillRepo.save(s));
    }

    @Transactional
    public AdminSkillView updateSkill(Long id, SkillForm f) {
        Skill s = skillRepo.findById(id).orElseThrow(() -> notFound("Skill not found"));
        String name = clean(f.name(), "Skill name");
        skillRepo.findByName(name).ifPresent(other -> {
            if (!other.getId().equals(id)) {
                throw conflict("A skill with this name already exists");
            }
        });
        s.setName(name);
        s.setCategory(f.category() == null ? null : f.category().trim());
        setPrerequisites(s, f.prerequisiteIds());
        return skillView(skillRepo.save(s));
    }

    @Transactional
    public void deleteSkill(Long id) {
        Skill s = skillRepo.findById(id).orElseThrow(() -> notFound("Skill not found"));
        if (roleSkillRepo.existsBySkillId(id)) {
            throw conflict("This skill is required by a career role. Remove it from the role first.");
        }
        if (studentSkillRepo.existsBySkillId(id)) {
            throw conflict("Students have this skill recorded, so it cannot be deleted.");
        }
        resourceRepo.deleteBySkillId(id);
        for (Skill other : skillRepo.findAll()) {
            if (other.getPrerequisites().removeIf(p -> p.getId().equals(id))) {
                skillRepo.save(other);
            }
        }
        skillRepo.flush();
        skillRepo.delete(s);
    }

    private void setPrerequisites(Skill s, List<Long> ids) {
        Set<Skill> set = new HashSet<>();
        if (ids != null) {
            for (Long pid : ids) {
                if (pid == null) {
                    throw bad("Prerequisite ids cannot be empty");
                }
                Skill p = skillRepo.findById(pid)
                        .orElseThrow(() -> bad("Unknown prerequisite skill id " + pid));
                if (s.getId() != null
                        && (p.getId().equals(s.getId())
                            || dependsOn(p, s.getId(), new HashSet<>()))) {
                    throw bad(p.getName() + " cannot be a prerequisite: it would create a loop");
                }
                set.add(p);
            }
        }
        s.getPrerequisites().clear();
        s.getPrerequisites().addAll(set);
    }

    // does "start" (directly or indirectly) require the skill with id targetId?
    private boolean dependsOn(Skill start, Long targetId, Set<Long> seen) {
        if (!seen.add(start.getId())) {
            return false;
        }
        for (Skill p : start.getPrerequisites()) {
            if (p.getId().equals(targetId) || dependsOn(p, targetId, seen)) {
                return true;
            }
        }
        return false;
    }

    // ---------- career roles ----------

    @Transactional
    public RoleDto createRole(RoleForm f) {
        String title = clean(f.title(), "Role title");
        checkTitleFree(title, null);
        CareerRole r = new CareerRole();
        r.setTitle(title);
        r.setDescription(cleanDescription(f.description()));
        return roleDto(roleRepo.save(r));
    }

    @Transactional
    public RoleDto updateRole(Long id, RoleForm f) {
        CareerRole r = roleRepo.findById(id).orElseThrow(() -> notFound("Role not found"));
        String title = clean(f.title(), "Role title");
        checkTitleFree(title, id);
        r.setTitle(title);
        r.setDescription(cleanDescription(f.description()));
        return roleDto(roleRepo.save(r));
    }

    @Transactional
    public void deleteRole(Long id) {
        CareerRole r = roleRepo.findById(id).orElseThrow(() -> notFound("Role not found"));
        historyRepo.deleteByCareerRoleId(id);
        roleSkillRepo.deleteByCareerRoleId(id);
        roleRepo.delete(r);
    }

    private void checkTitleFree(String title, Long ignoreId) {
        for (CareerRole r : roleRepo.findAll()) {
            if (r.getTitle().equalsIgnoreCase(title) && !r.getId().equals(ignoreId)) {
                throw conflict("A role with this title already exists");
            }
        }
    }

    // ---------- role requirements ----------

    @Transactional(readOnly = true)
    public List<RoleSkillView> roleSkills(Long roleId) {
        roleRepo.findById(roleId).orElseThrow(() -> notFound("Role not found"));
        return roleSkillRepo.findByCareerRoleId(roleId).stream()
                .map(this::roleSkillView).toList();
    }

    // replaces the whole requirement list of a role
    @Transactional
    public List<RoleSkillView> setRoleSkills(Long roleId, List<RoleSkillForm> forms) {
        CareerRole role = roleRepo.findById(roleId)
                .orElseThrow(() -> notFound("Role not found"));
        if (forms == null || forms.isEmpty()) {
            throw bad("Give at least one skill for the role");
        }

        Set<Long> seen = new HashSet<>();
        List<RoleSkill> fresh = new ArrayList<>();
        for (RoleSkillForm f : forms) {
            if (f.skillId() == null) {
                throw bad("Every entry needs a skillId");
            }
            if (!seen.add(f.skillId())) {
                throw bad("The same skill appears twice");
            }
            if (f.requiredLevel() < 1 || f.requiredLevel() > 5) {
                throw bad("requiredLevel must be from 1 to 5");
            }
            if (f.weight() <= 0 || f.weight() > 1) {
                throw bad("weight must be above 0 and at most 1");
            }
            Skill skill = skillRepo.findById(f.skillId())
                    .orElseThrow(() -> bad("Unknown skill id " + f.skillId()));
            RoleSkill rs = new RoleSkill();
            rs.setCareerRole(role);
            rs.setSkill(skill);
            rs.setRequiredLevel(f.requiredLevel());
            rs.setWeight(f.weight());
            fresh.add(rs);
        }

        roleSkillRepo.deleteByCareerRoleId(roleId);
        roleSkillRepo.flush();
        roleSkillRepo.saveAll(fresh);
        return fresh.stream().map(this::roleSkillView).toList();
    }

    // ---------- learning resources ----------

    @Transactional(readOnly = true)
    public List<AdminResourceView> resources(Long skillId) {
        List<Resource> list = skillId == null
                ? resourceRepo.findAll() : resourceRepo.findBySkillId(skillId);
        return list.stream().map(this::resourceView).toList();
    }

    @Transactional
    public AdminResourceView addResource(ResourceForm f) {
        if (f.skillId() == null) {
            throw bad("skillId is required");
        }
        Skill skill = skillRepo.findById(f.skillId())
                .orElseThrow(() -> bad("Unknown skill id " + f.skillId()));
        String title = clean(f.title(), "Resource title");
        String url = f.url() == null ? "" : f.url().trim();
        if (!(url.startsWith("https://") || url.startsWith("http://")) || url.length() > 500) {
            throw bad("url must start with http:// or https:// and be under 500 characters");
        }
        String type = (f.type() == null || f.type().isBlank())
                ? "ARTICLE" : f.type().trim().toUpperCase();
        if (!List.of("VIDEO", "ARTICLE", "COURSE").contains(type)) {
            throw bad("type must be VIDEO, ARTICLE or COURSE");
        }
        Resource r = new Resource();
        r.setSkill(skill);
        r.setTitle(title);
        r.setUrl(url);
        r.setType(type);
        return resourceView(resourceRepo.save(r));
    }

    @Transactional
    public void deleteResource(Long id) {
        Resource r = resourceRepo.findById(id)
                .orElseThrow(() -> notFound("Resource not found"));
        resourceRepo.delete(r);
    }

    // ---------- analytics ----------

    @Transactional(readOnly = true)
    public Analytics analytics() {
        List<User> students = userRepo.findByRole(Role.STUDENT);
        long total = students.size();

        Set<Long> required = roleSkillRepo.findAll().stream()
                .map(rs -> rs.getSkill().getId())
                .collect(Collectors.toSet());

        List<SkillGapCount> missing = skillRepo.findAll().stream()
                .filter(s -> required.contains(s.getId()))
                .map(s -> new SkillGapCount(s.getName(),
                        total - studentSkillRepo.countBySkillId(s.getId())))
                .sorted(Comparator.comparingLong(SkillGapCount::studentsWithoutSkill)
                        .reversed().thenComparing(SkillGapCount::skill))
                .limit(10)
                .toList();

        Map<Long, Map<Long, Integer>> levelsByStudent = new HashMap<>();
        for (User u : students) {
            Map<Long, Integer> lv = analysis.levels(u.getId());
            if (!lv.isEmpty()) {
                levelsByStudent.put(u.getId(), lv);
            }
        }

        List<RoleAverage> averages = new ArrayList<>();
        for (CareerRole r : roleRepo.findAll()) {
            List<RoleSkill> reqs = roleSkillRepo.findByCareerRoleId(r.getId());
            double sum = 0;
            for (Map<Long, Integer> lv : levelsByStudent.values()) {
                sum += analysis.readiness(reqs, lv);
            }
            double avg = levelsByStudent.isEmpty() ? 0
                    : Math.round(sum / levelsByStudent.size() * 10.0) / 10.0;
            averages.add(new RoleAverage(r.getTitle(), avg));
        }

        return new Analytics(total, levelsByStudent.size(), skillRepo.count(),
                roleRepo.count(), missing, averages);
    }

    // ---------- helpers ----------

    private AdminSkillView skillView(Skill s) {
        List<String> names = s.getPrerequisites().stream()
                .map(Skill::getName).sorted().toList();
        return new AdminSkillView(s.getId(), s.getName(), s.getCategory(), names);
    }

    private RoleDto roleDto(CareerRole r) {
        return new RoleDto(r.getId(), r.getTitle(), r.getDescription());
    }

    private RoleSkillView roleSkillView(RoleSkill rs) {
        return new RoleSkillView(rs.getSkill().getId(), rs.getSkill().getName(),
                rs.getRequiredLevel(), rs.getWeight());
    }

    private AdminResourceView resourceView(Resource r) {
        return new AdminResourceView(r.getId(), r.getSkill().getId(), r.getSkill().getName(),
                r.getTitle(), r.getUrl(), r.getType());
    }

    private String clean(String value, String label) {
        if (value == null || value.isBlank()) {
            throw bad(label + " is required");
        }
        String v = value.trim();
        if (v.length() > 100) {
            throw bad(label + " must be at most 100 characters");
        }
        return v;
    }

    private String cleanDescription(String value) {
        if (value == null) {
            return null;
        }
        String v = value.trim();
        if (v.length() > 1000) {
            throw bad("Description must be at most 1000 characters");
        }
        return v;
    }

    private ResponseStatusException bad(String m) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, m);
    }

    private ResponseStatusException notFound(String m) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, m);
    }

    private ResponseStatusException conflict(String m) {
        return new ResponseStatusException(HttpStatus.CONFLICT, m);
    }
}
