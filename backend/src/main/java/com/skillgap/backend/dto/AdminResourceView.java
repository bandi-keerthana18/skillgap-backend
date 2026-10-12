package com.skillgap.backend.dto;

public record AdminResourceView(Long id, Long skillId, String skill, String title,
                                String url, String type) {
}
