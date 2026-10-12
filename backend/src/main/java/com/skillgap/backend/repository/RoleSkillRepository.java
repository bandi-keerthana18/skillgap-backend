package com.skillgap.backend.repository;

import com.skillgap.backend.entity.RoleSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoleSkillRepository extends JpaRepository<RoleSkill, Long> {
    List<RoleSkill> findByCareerRoleId(Long roleId);
    boolean existsBySkillId(Long skillId);
    void deleteByCareerRoleId(Long roleId);
}
