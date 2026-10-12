
package com.skillgap.backend.dto;

import java.util.List;

public record RoadmapStep(int step, Long skillId, String skill, String status,
                          int currentLevel, int targetLevel, double priority,
                          List<String> prerequisites, List<ResourceDto> resources) {
}
