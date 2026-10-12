package com.skillgap.backend.dto;

import java.util.List;

public record GapReport(Long roleId, String roleTitle, double readinessScore,
                        double alignmentScore, List<GapItem> missing,
                        List<GapItem> weak, List<GapItem> strong) {
}
