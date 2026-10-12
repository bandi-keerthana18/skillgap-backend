package com.skillgap.backend.repository;

import com.skillgap.backend.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
    List<Resource> findBySkillId(Long skillId);
    void deleteBySkillId(Long skillId);
}
