package com.skillgap.backend.dto;

public record RankedRole(Long roleId, String title, double alignmentPercent,
                         double readinessPercent) {
}
