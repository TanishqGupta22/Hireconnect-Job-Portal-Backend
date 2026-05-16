package com.hireconnect.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AdminAnalytics {
    private Long totalUsers;
    private Long totalRecruiters;
    private Long totalCandidates;
    private Double totalRevenue;
    private Long activeJobs;
    private Double monthlyGrowth;
    private LocalDateTime lastUpdated;
    private Long totalApplications;
    private Long totalInterviews;
    private Long totalSubscriptions;
    private Long activeSubscriptions;
    private Double averageRevenuePerUser;
    private Long newUsersThisMonth;
    private Long newJobsThisMonth;
    private Long newApplicationsThisMonth;
    private Long completedInterviewsThisMonth;
    private Double revenueThisMonth;
}
