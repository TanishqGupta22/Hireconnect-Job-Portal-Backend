package com.hireconnect.notification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "profile-service")
public interface ProfileClient {

    @GetMapping("/profiles/candidates/available")
    List<CandidateProfileResponse> getAvailableCandidates();

    // Inner DTO or import if possible, but let's define it here for simplicity
    class CandidateProfileResponse {
        private Long userId;
        private List<String> skills;
        private Boolean jobAlertsEnabled;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public List<String> getSkills() { return skills; }
        public void setSkills(List<String> skills) { this.skills = skills; }
        public Boolean getJobAlertsEnabled() { return jobAlertsEnabled; }
        public void setJobAlertsEnabled(Boolean jobAlertsEnabled) { this.jobAlertsEnabled = jobAlertsEnabled; }
    }
}
