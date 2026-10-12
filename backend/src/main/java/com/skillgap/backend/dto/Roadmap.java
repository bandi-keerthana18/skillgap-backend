package com.skillgap.backend.dto;

import java.util.List;

public record Roadmap(Long roleId, String roleTitle, double readinessScore,
                      int totalSteps, List<RoadmapStep> steps) {
}
