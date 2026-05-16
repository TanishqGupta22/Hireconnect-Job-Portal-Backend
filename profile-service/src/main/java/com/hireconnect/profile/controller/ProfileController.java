package com.hireconnect.profile.controller;

import com.hireconnect.profile.dto.*;
import com.hireconnect.profile.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final ProfileService profileService;

    // Candidate Profile Endpoints
    @PostMapping("/candidate")
    public ResponseEntity<CandidateProfileResponse> createCandidateProfile(@Valid @RequestBody CandidateProfileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(profileService.createCandidateProfile(request));
    }
    
    @GetMapping("/candidate/{userId}")
    public ResponseEntity<CandidateProfileResponse> getCandidateProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.getCandidateProfile(userId));
    }
    
    @PutMapping("/candidate/{userId}")
    public ResponseEntity<CandidateProfileResponse> updateCandidateProfile(@PathVariable Long userId, @Valid @RequestBody CandidateProfileRequest request) {
        return ResponseEntity.ok(profileService.updateCandidateProfile(userId, request));
    }

    @DeleteMapping("/candidate/{userId}")
    public ResponseEntity<Void> deleteCandidateProfile(@PathVariable Long userId) {
        profileService.deleteCandidateProfile(userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/candidate/{userId}/job-alerts")
    public ResponseEntity<Void> toggleJobAlerts(@PathVariable Long userId, @RequestParam Boolean enabled) {
        profileService.toggleJobAlerts(userId, enabled);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/candidate/{userId}/view")
    public ResponseEntity<Void> viewCandidateProfile(
            @PathVariable Long userId,
            @RequestParam Long viewerId,
            @RequestParam String viewerName) {
        profileService.viewCandidateProfile(userId, viewerId, viewerName);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/candidates/public")
    public ResponseEntity<List<CandidateProfileResponse>> getPublicCandidateProfiles() {
        return ResponseEntity.ok(profileService.getPublicCandidateProfiles());
    }

    @GetMapping("/candidates/available")
    public ResponseEntity<List<CandidateProfileResponse>> getAvailableCandidateProfiles() {
        return ResponseEntity.ok(profileService.getAvailableCandidateProfiles());
    }

    // Recruiter Profile Endpoints
    @PostMapping("/recruiter")
    public ResponseEntity<RecruiterProfileResponse> createRecruiterProfile(@Valid @RequestBody RecruiterProfileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(profileService.createRecruiterProfile(request));
    }
    
    @GetMapping("/recruiter/{userId}")
    public ResponseEntity<RecruiterProfileResponse> getRecruiterProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.getRecruiterProfile(userId));
    }
    
    @PutMapping("/recruiter/{userId}")
    public ResponseEntity<RecruiterProfileResponse> updateRecruiterProfile(@PathVariable Long userId, @Valid @RequestBody RecruiterProfileRequest request) {
        return ResponseEntity.ok(profileService.updateRecruiterProfile(userId, request));
    }

    @DeleteMapping("/recruiter/{userId}")
    public ResponseEntity<Void> deleteRecruiterProfile(@PathVariable Long userId) {
        profileService.deleteRecruiterProfile(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/recruiters/active")
    public ResponseEntity<List<RecruiterProfileResponse>> getActiveRecruiterProfiles() {
        return ResponseEntity.ok(profileService.getActiveRecruiterProfiles());
    }

    @GetMapping("/recruiters/verified")
    public ResponseEntity<List<RecruiterProfileResponse>> getVerifiedRecruiterProfiles() {
        return ResponseEntity.ok(profileService.getVerifiedRecruiterProfiles());
    }

    // Generic Profile Endpoints
    @GetMapping
    public ResponseEntity<List<Object>> getAllProfiles() {
        try {
            List<Object> profiles = profileService.getAllProfiles();
            return ResponseEntity.ok(profiles);
        } catch (Exception e) {
            log.error("Error fetching all profiles: {}", e.getMessage());
            return ResponseEntity.ok(new java.util.ArrayList<>());
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getProfileByUserId(@PathVariable Long userId) {
        // Try to get candidate profile first
        try {
            return ResponseEntity.ok(profileService.getCandidateProfile(userId));
        } catch (Exception e) {
            // If candidate profile not found, try recruiter profile
            try {
                return ResponseEntity.ok(profileService.getRecruiterProfile(userId));
            } catch (Exception ex) {
                return ResponseEntity.notFound().build();
            }
        }
    }
}
