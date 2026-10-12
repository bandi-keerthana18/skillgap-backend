package com.skillgap.backend.dto;

import java.util.List;

public record SkillForm(String name, String category, List<Long> prerequisiteIds) {
}
