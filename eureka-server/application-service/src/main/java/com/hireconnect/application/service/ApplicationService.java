package com.hireconnect.application.service;

import com.hireconnect.application.dto.ApplicationRequest;
import com.hireconnect.application.dto.ApplicationResponse;
import com.hireconnect.application.dto.ApplicationStatusUpdateRequest;
import com.hireconnect.application.entity.Application;
import com.hireconnect.application.entity.ApplicationStatus;
import com.hireconnect.application.exception.ApplicationNotFoundException;
import com.hireconnect.application.exception.DuplicateApplicationException;
import com.hireconnect.application.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final com.hireconnect.application.client.SubscriptionClient subscriptionClient;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public ApplicationResponse applyForJob(ApplicationRequest request) {
        // Check subscription limit
        Boolean canApply = subscriptionClient.checkLimit(request.getCandidateId(), "CANDIDATE");
        if (Boolean.FALSE.equals(canApply)) {
            throw new IllegalStateException("You have reached your job application limit. Please upgrade your subscription.");
        }

        // Check if candidate has already applied for this job
        if (applicationRepository.existsByJobIdAndCandidateId(request.getJobId(), request.getCandidateId())) {
            throw new DuplicateApplicationException("You have already applied for this job");
        }

        Application application = Application.builder()
                .jobId(request.getJobId())
                .candidateId(request.getCandidateId())
                .recruiterId(request.getRecruiterId())
                .jobTitle(request.getJobTitle())
                .companyName(request.getCompanyName())
                .candidateName(request.getCandidateName())
                .candidateEmail(request.getCandidateEmail())
                .resumeUrl(request.getResumeUrl())
                .coverLetter(request.getCoverLetter())
                .status(ApplicationStatus.APPLIED)
                .appliedAt(LocalDateTime.now())
                .isShortlisted(false)
                .isArchived(false)
                .build();

        application = applicationRepository.save(application);
        
        // Increment usage
        subscriptionClient.incrementUsage(application.getCandidateId(), "CANDIDATE");
        
        // Send notification event
        sendApplicationEvent("application.created", application);
        
        log.info("Applied for job ID: {} by candidate ID: {} for recruiter ID: {}", request.getJobId(), request.getCandidateId(), request.getRecruiterId());

        return mapToResponse(application);
    }

    @Transactional
    public void withdrawApplication(Long applicationId, Long candidateId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found with ID: " + applicationId));

        if (!application.getCandidateId().equals(candidateId)) {
            throw new IllegalArgumentException("You can only withdraw your own applications");
        }

        if (application.getStatus() == ApplicationStatus.WITHDRAWN) {
            throw new IllegalArgumentException("Application is already withdrawn");
        }

        application.setStatus(ApplicationStatus.WITHDRAWN);
        application.setUpdatedAt(LocalDateTime.now());

        applicationRepository.save(application);
        
        // Send notification event
        sendApplicationEvent("application.withdrawn", application);
        
        log.info("Application {} withdrawn by candidate {}", applicationId, candidateId);
    }

    public ApplicationResponse getApplicationById(Long id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found with ID: " + id));
        
        return mapToResponse(application);
    }

    @Transactional
    public ApplicationResponse updateApplicationStatus(Long id, ApplicationStatusUpdateRequest request) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found with ID: " + id));

        ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(request.getStatus());
        application.setNotes(request.getNotes());
        application.setRecruiterNotes(request.getRecruiterNotes());
        application.setReviewedBy(request.getReviewedBy());
        application.setReviewedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());

        if (request.getRating() != null) {
            application.setRating(request.getRating());
        }

        if (request.getRejectionReason() != null) {
            application.setRejectionReason(request.getRejectionReason());
        }

        // Handle interview scheduling
        if (request.getStatus() == ApplicationStatus.INTERVIEW_SCHEDULED) {
            application.setInterviewScheduledAt(LocalDateTime.now());
            application.setInterviewMode(request.getInterviewMode());
            application.setInterviewLocation(request.getInterviewLocation());
            application.setInterviewLink(request.getInterviewLink());
        }

        // Handle shortlisting
        if (request.getStatus() == ApplicationStatus.SHORTLISTED) {
            application.setIsShortlisted(true);
        }

        application = applicationRepository.save(application);

        // Send status change notification
        if (!oldStatus.equals(request.getStatus())) {
            sendApplicationEvent("application.status.changed", application);
        }

        log.info("Updated application {} status to {} by reviewer {}", id, request.getStatus(), request.getReviewedBy());

        return mapToResponse(application);
    }

    public Page<ApplicationResponse> getCandidateApplications(Long candidateId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appliedAt"));
        Page<Application> applications = applicationRepository.findByCandidateId(candidateId, pageable);
        
        return applications.map(this::mapToResponse);
    }

    public Page<ApplicationResponse> getCandidateApplicationsByStatus(Long candidateId, ApplicationStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appliedAt"));
        Page<Application> applications = applicationRepository.findByCandidateIdAndStatus(candidateId, status, pageable);
        
        return applications.map(this::mapToResponse);
    }

    public Page<ApplicationResponse> getJobApplications(Long jobId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appliedAt"));
        Page<Application> applications = applicationRepository.findByJobId(jobId, pageable);
        
        return applications.map(this::mapToResponse);
    }

    public Page<ApplicationResponse> getJobApplicationsByStatus(Long jobId, ApplicationStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appliedAt"));
        Page<Application> applications = applicationRepository.findByJobIdAndStatus(jobId, status, pageable);
        
        return applications.map(this::mapToResponse);
    }

    public Page<ApplicationResponse> getRecruiterApplications(Long recruiterId, int page, int size) {
        log.info("Fetching applications for recruiter ID: {}, page: {}, size: {}", recruiterId, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appliedAt"));
        Page<Application> applications = applicationRepository.findByRecruiterId(recruiterId, pageable);
        
        if (applications == null) {
            log.warn("Application repository returned null for recruiter ID: {}", recruiterId);
            return Page.empty();
        }
        
        log.info("Found {} applications for recruiter ID: {}", applications.getTotalElements(), recruiterId);
        return applications.map(this::mapToResponse);
    }

    public Page<ApplicationResponse> getRecruiterApplicationsByStatus(Long recruiterId, ApplicationStatus status, int page, int size) {
        log.info("Searching applications for recruiter ID: {} with status: {}", recruiterId, status);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appliedAt"));
        Page<Application> applications = applicationRepository.findByRecruiterIdAndStatus(recruiterId, status, pageable);
        return applications.map(this::mapToResponse);
    }

    public List<ApplicationResponse> getShortlistedApplications(Long jobId) {
        List<Application> applications = applicationRepository.findByJobIdAndIsShortlistedTrue(jobId);
        return applications.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public long getTotalApplicationsForJob(Long jobId) {
        return applicationRepository.countByJobId(jobId);
    }

    public long getApplicationsCountByStatus(Long jobId, ApplicationStatus status) {
        return applicationRepository.countByJobIdAndStatus(jobId, status);
    }

    public long getTotalApplicationsForRecruiter(Long recruiterId) {
        return applicationRepository.countByRecruiterId(recruiterId);
    }

    public long getRecruiterApplicationsCountByStatus(Long recruiterId, ApplicationStatus status) {
        return applicationRepository.countByRecruiterIdAndStatus(recruiterId, status);
    }

    public long getTotalApplicationsForCandidate(Long candidateId) {
        return applicationRepository.countByCandidateId(candidateId);
    }

    public Page<ApplicationResponse> getAllApplications(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appliedAt"));
        Page<Application> applications = applicationRepository.findAll(pageable);
        
        return applications.map(this::mapToResponse);
    }

    @Transactional
    public void archiveApplication(Long id, Long userId) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found with ID: " + id));

        // Only recruiter or candidate can archive
        if (!application.getCandidateId().equals(userId) && !application.getReviewedBy().equals(userId)) {
            throw new IllegalArgumentException("You can only archive your own applications or applications you reviewed");
        }

        application.setIsArchived(true);
        application.setUpdatedAt(LocalDateTime.now());

        applicationRepository.save(application);
        log.info("Archived application {}", id);
    }

    private void sendApplicationEvent(String eventType, Application application) {
        try {
            rabbitTemplate.convertAndSend("notification.exchange", eventType, application);
            log.info("Sent {} event for application {}", eventType, application.getId());
        } catch (Exception e) {
            log.error("Failed to send {} event for application {}", eventType, application.getId(), e);
        }
    }

    private ApplicationResponse mapToResponse(Application application) {
        return ApplicationResponse.builder()
                .id(application.getId())
                .jobId(application.getJobId())
                .candidateId(application.getCandidateId())
                .recruiterId(application.getRecruiterId())
                .jobTitle(application.getJobTitle())
                .companyName(application.getCompanyName())
                .candidateName(application.getCandidateName())
                .candidateEmail(application.getCandidateEmail())
                .resumeUrl(application.getResumeUrl())
                .coverLetter(application.getCoverLetter())
                .status(application.getStatus())
                .appliedAt(application.getAppliedAt())
                .updatedAt(application.getUpdatedAt())
                .reviewedAt(application.getReviewedAt())
                .reviewedBy(application.getReviewedBy())
                .notes(application.getNotes())
                .recruiterNotes(application.getRecruiterNotes())
                .rating(application.getRating())
                .isShortlisted(application.getIsShortlisted())
                .isArchived(application.getIsArchived())
                .rejectionReason(application.getRejectionReason())
                .interviewScheduledAt(application.getInterviewScheduledAt())
                .interviewMode(application.getInterviewMode())
                .interviewLocation(application.getInterviewLocation())
                .interviewLink(application.getInterviewLink())
                .build();
    }
}
