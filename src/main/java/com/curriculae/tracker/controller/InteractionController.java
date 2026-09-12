package com.curriculae.tracker.controller;

import com.curriculae.tracker.model.Interaction;
import com.curriculae.tracker.service.InteractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interactions")
@RequiredArgsConstructor
@Tag(name = "Interactions", description = "Endpoints for logging and querying interactions")
public class InteractionController {

    private final InteractionService service;

    @GetMapping
    @Operation(summary = "List all interactions (most recent first)")
    public ResponseEntity<List<Interaction>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/by-application/{applicationId}")
    @Operation(summary = "List interactions for a specific job application")
    public ResponseEntity<List<Interaction>> byApplication(@PathVariable Long applicationId) {
        return ResponseEntity.ok(service.findByApplicationId(applicationId));
    }

    @GetMapping("/by-recruiter/{recruiterId}")
    @Operation(summary = "List interactions for a specific recruiter")
    public ResponseEntity<List<Interaction>> byRecruiter(@PathVariable Long recruiterId) {
        return ResponseEntity.ok(service.findByRecruiterId(recruiterId));
    }

    @PostMapping
    @Operation(summary = "Log a new interaction (auto-updates parent entity)")
    public ResponseEntity<Interaction> create(@RequestBody Interaction interaction) {
        Interaction saved = service.logInteraction(interaction);
        service.autoUpdateStatus(interaction);
        return ResponseEntity.ok(saved);
    }
}
