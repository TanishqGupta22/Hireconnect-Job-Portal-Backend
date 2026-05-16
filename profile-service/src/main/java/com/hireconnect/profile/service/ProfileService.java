package com.hireconnect.profile.service;

import com.hireconnect.profile.dto.CandidateProfileRequest;
import com.hireconnect.profile.dto.CandidateProfileResponse;
import com.hireconnect.profile.dto.RecruiterProfileRequest;
import com.hireconnect.profile.dto.RecruiterProfileResponse;
import com.hireconnect.profile.entity.CandidateProfile;
import com.hireconnect.profile.entity.RecruiterProfile;
import com.hireconnect.profile.exception.ProfileNotFoundException;
import com.hireconnect.profile.repository.CandidateProfileRepository;
import com.hireconnect.profile.repository.RecruiterProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final CandidateProfileRepository candidateProfileRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    // Candidate Profile Methods
    @Transactional
    @CacheEvict(value = "candidateProfiles", allEntries = true)
    public CandidateProfileResponse createCandidateProfile(CandidateProfileRequest request) {
        if (candidateProfileRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("Candidate profile already exists for user ID: " + request.getUserId());
        }

        CandidateProfile profile = CandidateProfile.builder()
                .userId(request.getUserId())
                .fullName(request.getFullName())
                .headline(request.getHeadline())
                .mobile(request.getMobile())
                .skills(request.getSkills())
                .experience(request.getExperience())
                .education(request.getEducation())
                .resumeUrl(request.getResumeUrl())
                .profileImageUrl(request.getProfileImageUrl())
                .address(request.getAddress())
                .linkedin(request.getLinkedin())
                .github(request.getGithub())
                .portfolio(request.getPortfolio())
                .bio(request.getBio())
                .isPublic(request.getIsPublic())
                .isAvailable(request.getIsAvailable())
                .jobAlertsEnabled(request.getJobAlertsEnabled() != null ? request.getJobAlertsEnabled() : false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        profile = candidateProfileRepository.save(profile);
        log.info("Created candidate profile for user ID: {}", request.getUserId());

        return mapToCandidateResponse(profile);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "candidateProfiles", key = "#userId")
    public CandidateProfileResponse getCandidateProfile(Long userId) {
        CandidateProfile profile = candidateProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException("Candidate profile not found for user ID: " + userId));
        
        return mapToCandidateResponse(profile);
    }

    @Transactional
    public void viewCandidateProfile(Long candidateId, Long viewerId, String viewerName) {
        log.info("User {} is viewing profile of candidate {}", viewerId, candidateId);
        
        // In a real app, we might check if candidate exists
        if (!candidateProfileRepository.existsByUserId(candidateId)) {
            throw new ProfileNotFoundException("Candidate profile not found for user ID: " + candidateId);
        }

        // Send profile view event to RabbitMQ
        java.util.Map<String, Object> event = new java.util.HashMap<>();
        event.put("candidateId", candidateId);
        event.put("viewerId", viewerId);
        event.put("viewerName", viewerName);
        event.put("viewedAt", LocalDateTime.now());

        try {
            rabbitTemplate.convertAndSend("notification.exchange", "profile.viewed", event);
            log.info("Sent profile.viewed event for candidate {}", candidateId);
        } catch (Exception e) {
            log.error("Failed to send profile.viewed event", e);
        }
    }

    @Transactional
    @CacheEvict(value = "candidateProfiles", key = "#userId")
    public CandidateProfileResponse updateCandidateProfile(Long userId, CandidateProfileRequest request) {
        CandidateProfile profile = candidateProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException("Candidate profile not found for user ID: " + userId));

        profile.setFullName(request.getFullName());
        profile.setHeadline(request.getHeadline());
        profile.setMobile(request.getMobile());
        profile.setSkills(request.getSkills());
        profile.setExperience(request.getExperience());
        profile.setEducation(request.getEducation());
        profile.setResumeUrl(request.getResumeUrl());
        profile.setProfileImageUrl(request.getProfileImageUrl());
        profile.setAddress(request.getAddress());
        profile.setLinkedin(request.getLinkedin());
        profile.setGithub(request.getGithub());
        profile.setPortfolio(request.getPortfolio());
        profile.setBio(request.getBio());
        profile.setIsPublic(request.getIsPublic());
        profile.setIsAvailable(request.getIsAvailable());
        if (request.getJobAlertsEnabled() != null) {
            profile.setJobAlertsEnabled(request.getJobAlertsEnabled());
        }
        profile.setUpdatedAt(LocalDateTime.now());

        profile = candidateProfileRepository.save(profile);
        log.info("Updated candidate profile for user ID: {}", userId);

        return mapToCandidateResponse(profile);
    }

    public List<CandidateProfileResponse> getPublicCandidateProfiles() {
        List<CandidateProfile> profiles = candidateProfileRepository.findByIsPublicTrue();
        return profiles.stream()
                .map(this::mapToCandidateResponse)
                .collect(Collectors.toList());
    }

    public List<CandidateProfileResponse> getAvailableCandidateProfiles() {
        List<CandidateProfile> profiles = candidateProfileRepository.findByIsAvailableTrueAndIsPublicTrue();
        return profiles.stream()
                .map(this::mapToCandidateResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "candidateProfiles", key = "#userId", allEntries = true)
    public void deleteCandidateProfile(Long userId) {
        if (!candidateProfileRepository.existsByUserId(userId)) {
            throw new ProfileNotFoundException("Candidate profile not found for user ID: " + userId);
        }
        candidateProfileRepository.deleteByUserId(userId);
        log.info("Deleted candidate profile for user ID: {}", userId);
    }

    @Transactional
    public void toggleJobAlerts(Long userId, Boolean enabled) {
        CandidateProfile profile = candidateProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException("Candidate profile not found for user ID: " + userId));
        profile.setJobAlertsEnabled(enabled);
        profile.setUpdatedAt(LocalDateTime.now());
        candidateProfileRepository.save(profile);
        log.info("Job alerts {} for user ID: {}", enabled ? "enabled" : "disabled", userId);
    }

    // Recruiter Profile Methods
    @Transactional
    @CacheEvict(value = "recruiterProfiles", allEntries = true)
    public RecruiterProfileResponse createRecruiterProfile(RecruiterProfileRequest request) {
        if (recruiterProfileRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("Recruiter profile already exists for user ID: " + request.getUserId());
        }

        RecruiterProfile profile = RecruiterProfile.builder()
                .userId(request.getUserId())
                .companyName(request.getCompanyName())
                .website(request.getWebsite())
                .industry(request.getIndustry())
                .companySize(request.getCompanySize())
                .description(request.getDescription())
                .logoUrl(request.getLogoUrl())
                .address(request.getAddress())
                .linkedin(request.getLinkedin())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .isActive(request.getIsActive())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        profile = recruiterProfileRepository.save(profile);
        log.info("Created recruiter profile for user ID: {}", request.getUserId());

        return mapToRecruiterResponse(profile);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "recruiterProfiles", key = "#userId")
    public RecruiterProfileResponse getRecruiterProfile(Long userId) {
        RecruiterProfile profile = recruiterProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException("Recruiter profile not found for user ID: " + userId));
        
        return mapToRecruiterResponse(profile);
    }

    @Transactional
    @CacheEvict(value = "recruiterProfiles", key = "#userId", allEntries = true)
    public RecruiterProfileResponse updateRecruiterProfile(Long userId, RecruiterProfileRequest request) {
        RecruiterProfile profile = recruiterProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException("Recruiter profile not found for user ID: " + userId));

        profile.setCompanyName(request.getCompanyName());
        profile.setWebsite(request.getWebsite());
        profile.setIndustry(request.getIndustry());
        profile.setCompanySize(request.getCompanySize());
        profile.setDescription(request.getDescription());
        profile.setLogoUrl(request.getLogoUrl());
        profile.setAddress(request.getAddress());
        profile.setLinkedin(request.getLinkedin());
        profile.setContactEmail(request.getContactEmail());
        profile.setContactPhone(request.getContactPhone());
        profile.setIsActive(request.getIsActive());
        profile.setUpdatedAt(LocalDateTime.now());

        profile = recruiterProfileRepository.save(profile);
        log.info("Updated recruiter profile for user ID: {}", userId);

        return mapToRecruiterResponse(profile);
    }

    public List<RecruiterProfileResponse> getActiveRecruiterProfiles() {
        List<RecruiterProfile> profiles = recruiterProfileRepository.findByIsActiveTrue();
        return profiles.stream()
                .map(this::mapToRecruiterResponse)
                .collect(Collectors.toList());
    }

    public List<RecruiterProfileResponse> getVerifiedRecruiterProfiles() {
        List<RecruiterProfile> profiles = recruiterProfileRepository.findByIsVerifiedTrue();
        return profiles.stream()
                .map(this::mapToRecruiterResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "recruiterProfiles", key = "#userId", allEntries = true)
    public void deleteRecruiterProfile(Long userId) {
        if (!recruiterProfileRepository.existsByUserId(userId)) {
            throw new ProfileNotFoundException("Recruiter profile not found for user ID: " + userId);
        }
        recruiterProfileRepository.deleteByUserId(userId);
        log.info("Deleted recruiter profile for user ID: {}", userId);
    }

    // Generic Profile Methods
    @Transactional(readOnly = true)
    public List<Object> getAllProfiles() {
        List<Object> allProfiles = new java.util.ArrayList<>();
        
        // Add all candidate profiles
        List<CandidateProfile> candidateProfiles = candidateProfileRepository.findAll();
        allProfiles.addAll(candidateProfiles.stream()
                .map(this::mapToCandidateResponse)
                .collect(Collectors.toList()));
        
        // Add all recruiter profiles
        List<RecruiterProfile> recruiterProfiles = recruiterProfileRepository.findAll();
        allProfiles.addAll(recruiterProfiles.stream()
                .map(this::mapToRecruiterResponse)
                .collect(Collectors.toList()));
        
        return allProfiles;
    }

    // Helper Methods
    private CandidateProfileResponse mapToCandidateResponse(CandidateProfile profile) {
        return new CandidateProfileResponse(
                profile.getId(),
                profile.getUserId(),
                profile.getFullName(),
                profile.getHeadline(),
                profile.getMobile(),
                profile.getSkills() != null ? new java.util.ArrayList<>(profile.getSkills()) : new java.util.ArrayList<>(),
                profile.getExperience() != null ? new java.util.ArrayList<>(profile.getExperience()) : new java.util.ArrayList<>(),
                profile.getEducation() != null ? new java.util.ArrayList<>(profile.getEducation()) : new java.util.ArrayList<>(),
                profile.getResumeUrl(),
                profile.getProfileImageUrl(),
                profile.getAddress(),
                profile.getLinkedin(),
                profile.getGithub(),
                profile.getPortfolio(),
                profile.getBio(),
                profile.getCreatedAt(),
                profile.getUpdatedAt(),
                profile.getIsPublic(),
                profile.getIsAvailable(),
                profile.getJobAlertsEnabled()
        );
    }

    private RecruiterProfileResponse mapToRecruiterResponse(RecruiterProfile profile) {
        return new RecruiterProfileResponse(
                profile.getId(),
                profile.getUserId(),
                profile.getCompanyName(),
                profile.getWebsite(),
                profile.getIndustry(),
                profile.getCompanySize(),
                profile.getDescription(),
                profile.getLogoUrl(),
                profile.getAddress(),
                profile.getLinkedin(),
                profile.getContactEmail(),
                profile.getContactPhone(),
                profile.getCreatedAt(),
                profile.getUpdatedAt(),
                profile.getIsVerified(),
                profile.getIsActive()
        );
    }
}
