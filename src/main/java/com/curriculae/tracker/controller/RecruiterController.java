package com.curriculae.tracker.controller;

import com.curriculae.tracker.model.Recruiter;
import com.curriculae.tracker.service.RecruiterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recruiters")
@RequiredArgsConstructor
@Tag(name = "Recruiters", description = "CRUD endpoints for CRM recruiter leads")
public class RecruiterController {

    private final RecruiterService service;

    @GetMapping
    @Operation(summary = "List all recruiters")
    public ResponseEntity<List<Recruiter>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a recruiter by ID")
    public ResponseEntity<Recruiter> getById(@PathVariable Long id) {
        Recruiter rec = service.findById(id);
        return rec != null ? ResponseEntity.ok(rec) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(summary = "Add a new recruiter lead")
    public ResponseEntity<Recruiter> create(@RequestBody Recruiter recruiter) {
        return ResponseEntity.ok(service.save(recruiter));
    }

    @PutMapping("/{id}/response")
    @Operation(summary = "Update a recruiter's response status")
    public ResponseEntity<Void> updateResponse(
            @PathVariable Long id,
            @RequestParam String response,
            @RequestParam(required = false) String notes) {
        service.updateResponse(id, response, notes);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a recruiter and its interactions")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
