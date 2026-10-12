package com.skillgap.backend.dto;


public record GapItem(Long skillId, String skill, int requiredLevel, int studentLevel,
                      int gap, double weight, String status) {
}
