package com.skillgap.backend.dto;

import java.util.List;

public record AdminSkillView(Long id, String name, String category, List<String> prerequisites) {
}
