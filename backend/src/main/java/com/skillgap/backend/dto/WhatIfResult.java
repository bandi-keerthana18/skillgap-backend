package com.skillgap.backend.dto;

public record WhatIfResult(Long roleId, String roleTitle,
                           double readinessBefore, double readinessAfter,
                           double alignmentBefore, double alignmentAfter,
                           double readinessChange, double alignmentChange) {
}
