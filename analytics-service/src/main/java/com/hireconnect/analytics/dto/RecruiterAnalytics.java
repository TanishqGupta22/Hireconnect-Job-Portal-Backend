package com.hireconnect.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class RecruiterAnalytics {
    private Long recruiterId;
    private Long totalJobs;
    private Long totalViews;
    private Long totalApplications;
    private Long shortlisted;
    private Long offered;
    private Long rejected;
    private Double conversionRate;
    private LocalDateTime lastUpdated;
    private Long activeJobs;
    private Long expiredJobs;
    private Double averageTimeToHire;
    private Long totalInterviews;
    private Long completedInterviews;
    private Double interviewCompletionRate;
    private java.util.List<ApplicationTrend> applicationTrends;

    @Data
    @AllArgsConstructor
    public static class ApplicationTrend {
        private String date;
        private Long count;
    }
}
