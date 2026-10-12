package com.skillgap.backend.repository;

import com.skillgap.backend.entity.StudentSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudentSkillRepository extends JpaRepository<StudentSkill, Long> {
    List<StudentSkill> findByUserId(Long userId);
    boolean existsBySkillId(Long skillId);
    long countBySkillId(Long skillId);
}
