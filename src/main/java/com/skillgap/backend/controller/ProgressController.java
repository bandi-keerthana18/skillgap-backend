package com.skillgap.backend.controller;

import com.skillgap.backend.dto.HistoryPoint;
import com.skillgap.backend.dto.LearnResult;
import com.skillgap.backend.dto.Roadmap;
import com.skillgap.backend.dto.StudentSkillRequest;
import com.skillgap.backend.dto.WhatIfResult;
import com.skillgap.backend.service.ProgressService;
import com.skillgap.backend.service.RoadmapService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProgressController {

    private final RoadmapService roadmapService;
    private final ProgressService progressService;

    @GetMapping("/roadmap/{roleId}")
    public Roadmap roadmap(Authentication auth, @PathVariable Long roleId) {
        return roadmapService.build(auth.getName(), roleId);
    }

    @PostMapping("/progress/learn")

    public LearnResult learn(Authentication auth, @RequestBody StudentSkillRequest body) {
        return progressService.learn(auth.getName(), body);
    }

    @PostMapping("/progress/assess/{roleId}")
    public HistoryPoint assess(Authentication auth, @PathVariable Long roleId) {
        return progressService.assess(auth.getName(), roleId);
    }

    @GetMapping("/progress/history/{roleId}")
    public List<HistoryPoint> history(Authentication auth, @PathVariable Long roleId) {
        return progressService.history(auth.getName(), roleId);
    }

    @PostMapping("/analysis/{roleId}/whatif")
    public WhatIfResult whatIf(Authentication auth, @PathVariable Long roleId,
                               @RequestBody List<StudentSkillRequest> body) {
        return progressService.whatIf(auth.getName(), roleId, body);
    }
}
