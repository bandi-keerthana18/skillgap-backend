package com.skillgap.backend.dto;

import java.util.List;

public record Analytics(long students, long activeStudents, long skills, long roles,
                        List<SkillGapCount> mostMissingSkills,
                        List<RoleAverage> averageReadinessByRole) {
}
