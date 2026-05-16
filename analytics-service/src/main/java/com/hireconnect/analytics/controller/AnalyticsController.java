package com.hireconnect.analytics.controller;

import com.hireconnect.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping
    public ResponseEntity<?> health() {
        return ResponseEntity.ok("Analytics Service Running");
    }

    @GetMapping("/recruiter/{recruiterId}")
    public ResponseEntity<?> getRecruiterAnalytics(@PathVariable Long recruiterId) {
        return ResponseEntity.ok(analyticsService.getRecruiterAnalytics(recruiterId));
    }

    @GetMapping("/admin")
    public ResponseEntity<?> getAdminAnalytics() {
        return ResponseEntity.ok(analyticsService.getAdminAnalytics());
    }

    @GetMapping("/charts/job-postings")
    public ResponseEntity<?> getJobPostingChart(@RequestParam(defaultValue = "monthly") String period) {
        return ResponseEntity.ok(analyticsService.getJobPostingChart(period));
    }

    @GetMapping("/charts/applications")
    public ResponseEntity<?> getApplicationChart(@RequestParam(defaultValue = "monthly") String period) {
        return ResponseEntity.ok(analyticsService.getApplicationChart(period));
    }

    @GetMapping("/charts/revenue")
    public ResponseEntity<?> getRevenueChart(@RequestParam(defaultValue = "monthly") String period) {
        return ResponseEntity.ok(analyticsService.getRevenueChart(period));
    }

    @GetMapping("/charts/user-growth")
    public ResponseEntity<?> getUserGrowthChart(@RequestParam(defaultValue = "monthly") String period) {
        return ResponseEntity.ok(analyticsService.getUserGrowthChart(period));
    }

    @GetMapping("/charts/job-status-distribution")
    public ResponseEntity<?> getJobStatusDistribution() {
        return ResponseEntity.ok(analyticsService.getJobStatusDistribution());
    }

    @GetMapping("/charts/application-status-distribution")
    public ResponseEntity<?> getApplicationStatusDistribution() {
        return ResponseEntity.ok(analyticsService.getApplicationStatusDistribution());
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        return ResponseEntity.ok(analyticsService.getDashboardStats());
    }
}
