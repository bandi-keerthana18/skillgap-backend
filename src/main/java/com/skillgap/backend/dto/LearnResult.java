package com.skillgap.backend.dto;

public record LearnResult(Long skillId, String skill, int level, int xpGained, int totalXp) {
}
