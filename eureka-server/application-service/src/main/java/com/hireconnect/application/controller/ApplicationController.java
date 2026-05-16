package com.hireconnect.application.controller;

import com.hireconnect.application.dto.ApplicationRequest;
import com.hireconnect.application.dto.ApplicationResponse;
import com.hireconnect.application.dto.ApplicationStatusUpdateRequest;
import com.hireconnect.application.entity.ApplicationStatus;
import com.hireconnect.application.service.ApplicationService;
import com.hireconnect.application.repository.ApplicationRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final ApplicationRepository applicationRepository;

    @GetMapping({"", "/admin"})
    public ResponseEntity<Page<ApplicationResponse>> getAllApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(applicationService.getAllApplications(page, size));
    }

    @PostMapping("/apply")
    public ResponseEntity<ApplicationResponse> applyForJob(@Valid @RequestBody ApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationService.applyForJob(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplicationById(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getApplicationById(id));
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<ApplicationResponse> updateApplicationStatus(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationStatusUpdateRequest request) {
        return ResponseEntity.ok(applicationService.updateApplicationStatus(id, request));
    }

    @PutMapping("/withdraw/{id}")
    public ResponseEntity<Void> withdrawApplication(@PathVariable Long id, @RequestParam Long candidateId) {
        applicationService.withdrawApplication(id, candidateId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/archive/{id}")
    public ResponseEntity<Void> archiveApplication(@PathVariable Long id, @RequestParam Long userId) {
        applicationService.archiveApplication(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<Page<ApplicationResponse>> getCandidateApplications(
            @PathVariable Long candidateId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(applicationService.getCandidateApplications(candidateId, page, size));
    }

    @GetMapping("/candidate/{candidateId}/status/{status}")
    public ResponseEntity<Page<ApplicationResponse>> getCandidateApplicationsByStatus(
            @PathVariable Long candidateId,
            @PathVariable ApplicationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(applicationService.getCandidateApplicationsByStatus(candidateId, status, page, size));
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<Page<ApplicationResponse>> getJobApplications(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(applicationService.getJobApplications(jobId, page, size));
    }

    @GetMapping("/job/{jobId}/status/{status}")
    public ResponseEntity<Page<ApplicationResponse>> getJobApplicationsByStatus(
            @PathVariable Long jobId,
            @PathVariable ApplicationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(applicationService.getJobApplicationsByStatus(jobId, status, page, size));
    }

    @GetMapping("/job/{jobId}/shortlisted")
    public ResponseEntity<?> getShortlistedApplications(@PathVariable Long jobId) {
        return ResponseEntity.ok(applicationService.getShortlistedApplications(jobId));
    }

    @GetMapping("/job/{jobId}/count")
    public ResponseEntity<Long> getTotalApplicationsForJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(applicationService.getTotalApplicationsForJob(jobId));
    }

    @GetMapping("/job/{jobId}/count/{status}")
    public ResponseEntity<Long> getApplicationsCountByStatus(
            @PathVariable Long jobId,
            @PathVariable ApplicationStatus status) {
        return ResponseEntity.ok(applicationService.getApplicationsCountByStatus(jobId, status));
    }

    @GetMapping("/recruiter/{recruiterId}")
    public ResponseEntity<Page<ApplicationResponse>> getRecruiterApplications(
            @PathVariable Long recruiterId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(applicationService.getRecruiterApplications(recruiterId, page, size));
    }

    @GetMapping("/recruiter/{recruiterId}/status/{status}")
    public ResponseEntity<Page<ApplicationResponse>> getRecruiterApplicationsByStatus(
            @PathVariable Long recruiterId,
            @PathVariable ApplicationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(applicationService.getRecruiterApplicationsByStatus(recruiterId, status, page, size));
    }

    @GetMapping("/recruiter/{recruiterId}/count")
    public ResponseEntity<?> getTotalApplicationsForRecruiter(@PathVariable Long recruiterId) {
        try {
            return ResponseEntity.ok(applicationService.getTotalApplicationsForRecruiter(recruiterId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new java.util.HashMap<String, String>() {{
                        put("message", "Error fetching application count: " + e.getMessage());
                        put("type", e.getClass().getName());
                    }});
        }
    }

    @GetMapping("/recruiter/{recruiterId}/count/{status}")
    public ResponseEntity<Long> getRecruiterApplicationsCountByStatus(
            @PathVariable Long recruiterId,
            @PathVariable ApplicationStatus status) {
        return ResponseEntity.ok(applicationService.getRecruiterApplicationsCountByStatus(recruiterId, status));
    }

    @GetMapping("/candidate/{candidateId}/count")
    public ResponseEntity<Long> getTotalApplicationsForCandidate(@PathVariable Long candidateId) {
        return ResponseEntity.ok(applicationService.getTotalApplicationsForCandidate(candidateId));
    }

    @GetMapping("/debug/count")
    public ResponseEntity<?> debugCount() {
        return ResponseEntity.ok(applicationRepository.count());
    }
}
