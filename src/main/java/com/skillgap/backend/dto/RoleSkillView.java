package com.skillgap.backend.dto;

public record RoleSkillView(Long skillId, String skill, int requiredLevel, double weight) {
}
