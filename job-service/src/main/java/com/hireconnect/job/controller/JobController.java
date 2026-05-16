package com.hireconnect.job.controller;

import com.hireconnect.job.dto.JobRequest;
import com.hireconnect.job.dto.JobResponse;
import com.hireconnect.job.dto.JobSearchRequest;
import com.hireconnect.job.entity.JobStatus;
import com.hireconnect.job.entity.JobType;
import com.hireconnect.job.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<?> createJob(@Valid @RequestBody JobRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(jobService.createJob(request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new java.util.HashMap<String, String>() {{
                        put("message", "Error creating job: " + e.getMessage());
                        put("type", e.getClass().getName());
                    }});
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJobById(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobResponse> updateJob(@PathVariable Long id, @Valid @RequestBody JobRequest request) {
        return ResponseEntity.ok(jobService.updateJob(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id, @RequestParam Long recruiterId) {
        jobService.deleteJob(id, recruiterId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<JobResponse> updateJobStatus(
            @PathVariable Long id, 
            @RequestParam JobStatus status,
            @RequestParam Long recruiterId) {
        return ResponseEntity.ok(jobService.updateJobStatus(id, status, recruiterId));
    }

    @GetMapping
    public ResponseEntity<Page<JobResponse>> getAllActiveJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(jobService.getAllActiveJobs(page, size));
    }

    @GetMapping("/recruiter/{recruiterId}")
    public ResponseEntity<Page<JobResponse>> getJobsByRecruiter(
            @PathVariable Long recruiterId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(jobService.getJobsByRecruiter(recruiterId, page, size));
    }

    @PostMapping("/search")
    public ResponseEntity<Page<JobResponse>> searchJobs(@Valid @RequestBody JobSearchRequest searchRequest) {
        return ResponseEntity.ok(jobService.searchJobs(searchRequest));
    }

    @GetMapping("/featured")
    public ResponseEntity<Page<JobResponse>> getFeaturedJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(jobService.getFeaturedJobs(page, size));
    }

    @GetMapping("/remote")
    public ResponseEntity<Page<JobResponse>> getRemoteJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(jobService.getRemoteJobs(page, size));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<JobResponse>> getJobsByType(@PathVariable JobType type) {
        return ResponseEntity.ok(jobService.getJobsByType(type));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<JobResponse>> getJobsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(jobService.getJobsByCategory(category));
    }

    @PostMapping("/skills")
    public ResponseEntity<List<JobResponse>> getJobsBySkills(@RequestBody List<String> skills) {
        return ResponseEntity.ok(jobService.getJobsBySkills(skills));
    }

    // Admin Endpoints
    @GetMapping("/admin/all")
    public ResponseEntity<List<JobResponse>> getAllJobsForAdmin() {
        return ResponseEntity.ok(jobService.getAllActiveJobs(0, 1000).getContent());
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> adminDeleteJob(@PathVariable Long id) {
        jobService.adminDeleteJob(id);
        return ResponseEntity.noContent().build();
    }
}
