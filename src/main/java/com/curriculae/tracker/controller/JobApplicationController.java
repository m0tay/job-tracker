package com.curriculae.tracker.controller;

import com.curriculae.tracker.model.JobApplication;
import com.curriculae.tracker.service.JobApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Tag(name = "Job Applications", description = "CRUD endpoints for job applications")
public class JobApplicationController {

    private final JobApplicationService service;

    @GetMapping
    @Operation(summary = "List all job applications")
    public ResponseEntity<List<JobApplication>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a job application by ID")
    public ResponseEntity<JobApplication> getById(@PathVariable Long id) {
        JobApplication app = service.findById(id);
        return app != null ? ResponseEntity.ok(app) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(summary = "Create a new job application")
    public ResponseEntity<JobApplication> create(@RequestBody JobApplication application) {
        return ResponseEntity.ok(service.save(application));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing job application")
    public ResponseEntity<JobApplication> update(@PathVariable Long id, @RequestBody JobApplication application) {
        JobApplication existing = service.findById(id);
        if (existing == null) return ResponseEntity.notFound().build();
        application.setId(id);
        return ResponseEntity.ok(service.save(application));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a job application and its interactions")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/metrics")
    @Operation(summary = "Get dashboard KPI metrics")
    public ResponseEntity<Map<String, Long>> metrics() {
        return ResponseEntity.ok(service.getMetrics());
    }

    @GetMapping("/follow-up")
    @Operation(summary = "List applications needing follow-up (>7 days)")
    public ResponseEntity<List<JobApplication>> followUp() {
        return ResponseEntity.ok(service.findNeedingFollowUp());
    }
}
