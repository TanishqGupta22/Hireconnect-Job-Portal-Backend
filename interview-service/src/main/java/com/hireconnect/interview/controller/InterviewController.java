package com.hireconnect.interview.controller;

import com.hireconnect.interview.dto.InterviewRequest;
import com.hireconnect.interview.dto.InterviewResponse;
import com.hireconnect.interview.dto.InterviewUpdateRequest;
import com.hireconnect.interview.entity.InterviewStatus;
import com.hireconnect.interview.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @GetMapping
    public ResponseEntity<Page<InterviewResponse>> getAllInterviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(interviewService.getAllInterviews(page, size));
    }

    @PostMapping
    public ResponseEntity<InterviewResponse> scheduleInterview(@Valid @RequestBody InterviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(interviewService.scheduleInterview(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InterviewResponse> getInterviewById(@PathVariable Long id) {
        return ResponseEntity.ok(interviewService.getInterviewById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InterviewResponse> updateInterview(@PathVariable Long id, @Valid @RequestBody InterviewUpdateRequest request) {
        return ResponseEntity.ok(interviewService.updateInterview(id, request));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<InterviewResponse> confirmInterview(@PathVariable Long id) {
        return ResponseEntity.ok(interviewService.confirmInterview(id));
    }

    @PutMapping("/{id}/reschedule")
    public ResponseEntity<InterviewResponse> rescheduleInterview(@PathVariable Long id, @Valid @RequestBody InterviewUpdateRequest request) {
        return ResponseEntity.ok(interviewService.rescheduleInterview(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelInterview(@PathVariable Long id, @RequestParam String reason) {
        interviewService.cancelInterview(id, reason);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/recruiter/{recruiterId}")
    public ResponseEntity<Page<InterviewResponse>> getInterviewsByRecruiter(
            @PathVariable Long recruiterId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(interviewService.getInterviewsByRecruiter(recruiterId, page, size));
    }

    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<Page<InterviewResponse>> getInterviewsByCandidate(
            @PathVariable Long candidateId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(interviewService.getInterviewsByCandidate(candidateId, page, size));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<InterviewResponse>> getInterviewsByStatus(
            @PathVariable InterviewStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(interviewService.getInterviewsByStatus(status, page, size));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<InterviewResponse>> getUpcomingInterviews() {
        return ResponseEntity.ok(interviewService.getUpcomingInterviews());
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<InterviewResponse>> getOverdueInterviews() {
        return ResponseEntity.ok(interviewService.getOverdueInterviews());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<InterviewResponse>> getInterviewsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(interviewService.getInterviewsByUserId(userId));
    }

    @GetMapping("/recruiter/{recruiterId}/count/{status}")
    public ResponseEntity<Long> getInterviewsCountByRecruiterAndStatus(
            @PathVariable Long recruiterId,
            @PathVariable InterviewStatus status) {
        return ResponseEntity.ok(interviewService.getInterviewsCountByRecruiterAndStatus(recruiterId, status));
    }

    @GetMapping("/candidate/{candidateId}/count/{status}")
    public ResponseEntity<Long> getInterviewsCountByCandidateAndStatus(
            @PathVariable Long candidateId,
            @PathVariable InterviewStatus status) {
        return ResponseEntity.ok(interviewService.getInterviewsCountByCandidateAndStatus(candidateId, status));
    }
    @GetMapping("/application/{applicationId}")
    public ResponseEntity<InterviewResponse> getInterviewByApplicationId(@PathVariable Long applicationId) {
        return ResponseEntity.ok(interviewService.getInterviewByApplicationId(applicationId));
    }
}
