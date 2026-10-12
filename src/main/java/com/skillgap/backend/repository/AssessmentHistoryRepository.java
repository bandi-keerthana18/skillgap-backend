package com.skillgap.backend.repository;

import com.skillgap.backend.entity.AssessmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssessmentHistoryRepository extends JpaRepository<AssessmentHistory, Long> {
    List<AssessmentHistory> findByUserIdAndCareerRoleIdOrderByTakenAtAsc(Long userId, Long roleId);
    void deleteByCareerRoleId(Long roleId);
}
