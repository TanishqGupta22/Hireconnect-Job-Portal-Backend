package com.hireconnect.interview.service;

import com.hireconnect.interview.dto.InterviewRequest;
import com.hireconnect.interview.dto.InterviewResponse;
import com.hireconnect.interview.dto.InterviewUpdateRequest;
import com.hireconnect.interview.entity.Interview;
import com.hireconnect.interview.entity.InterviewStatus;
import com.hireconnect.interview.exception.InterviewNotFoundException;
import com.hireconnect.interview.exception.InterviewAlreadyScheduledException;
import com.hireconnect.interview.repository.InterviewRepository;
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
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public InterviewResponse scheduleInterview(InterviewRequest request) {
        // Check if interview is already scheduled for this application
        if (interviewRepository.existsByApplicationId(request.getApplicationId())) {
            throw new InterviewAlreadyScheduledException("Interview already scheduled for this application");
        }

        Interview interview = Interview.builder()
                .applicationId(request.getApplicationId())
                .recruiterId(request.getRecruiterId())
                .candidateId(request.getCandidateId())
                .scheduledAt(request.getScheduledAt())
                .mode(request.getMode())
                .meetingLink(request.getMeetingLink())
                .location(request.getLocation())
                .notes(request.getNotes())
                .status(InterviewStatus.SCHEDULED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .duration(request.getDuration())
                .interviewType(request.getInterviewType())
                .interviewerName(request.getInterviewerName())
                .candidateName(request.getCandidateName())
                .jobTitle(request.getJobTitle())
                .build();

        interview = interviewRepository.save(interview);

        // Send notification event
        sendInterviewEvent("interview.scheduled", interview);

        log.info("Scheduled interview for application ID: {} at {}", request.getApplicationId(), request.getScheduledAt());

        return mapToResponse(interview);
    }

    @Transactional
    public InterviewResponse confirmInterview(Long id) {
        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> new InterviewNotFoundException("Interview not found with ID: " + id));

        if (interview.getStatus() != InterviewStatus.SCHEDULED && interview.getStatus() != InterviewStatus.RESCHEDULED) {
            throw new IllegalArgumentException("Interview can only be confirmed if it's scheduled or rescheduled");
        }

        interview.setStatus(InterviewStatus.CONFIRMED);
        interview.setConfirmedAt(LocalDateTime.now());
        interview.setUpdatedAt(LocalDateTime.now());

        interview = interviewRepository.save(interview);

        // Send notification event
        sendInterviewEvent("interview.confirmed", interview);

        log.info("Confirmed interview with ID: {}", id);

        return mapToResponse(interview);
    }

    @Transactional
    public InterviewResponse rescheduleInterview(Long id, InterviewUpdateRequest request) {
        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> new InterviewNotFoundException("Interview not found with ID: " + id));

        if (interview.getStatus() == InterviewStatus.COMPLETED || interview.getStatus() == InterviewStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot reschedule completed or cancelled interview");
        }

        interview.setScheduledAt(request.getScheduledAt());
        interview.setMode(request.getMode());
        interview.setMeetingLink(request.getMeetingLink());
        interview.setLocation(request.getLocation());
        interview.setNotes(request.getNotes());
        interview.setStatus(InterviewStatus.RESCHEDULED);
        interview.setRescheduledAt(LocalDateTime.now());
        interview.setUpdatedAt(LocalDateTime.now());

        interview = interviewRepository.save(interview);

        // Send notification event
        sendInterviewEvent("interview.rescheduled", interview);

        log.info("Rescheduled interview with ID: {} to {}", id, request.getScheduledAt());

        return mapToResponse(interview);
    }

    @Transactional
    public InterviewResponse updateInterview(Long id, InterviewUpdateRequest request) {
        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> new InterviewNotFoundException("Interview not found with ID: " + id));

        if (request.getScheduledAt() != null) {
            interview.setScheduledAt(request.getScheduledAt());
        }
        if (request.getMode() != null) {
            interview.setMode(request.getMode());
        }
        if (request.getMeetingLink() != null) {
            interview.setMeetingLink(request.getMeetingLink());
        }
        if (request.getLocation() != null) {
            interview.setLocation(request.getLocation());
        }
        if (request.getNotes() != null) {
            interview.setNotes(request.getNotes());
        }
        if (request.getStatus() != null) {
            interview.setStatus(request.getStatus());
            
            if (request.getStatus() == InterviewStatus.COMPLETED) {
                interview.setCompletedAt(LocalDateTime.now());
            } else if (request.getStatus() == InterviewStatus.CANCELLED) {
                interview.setCancelledAt(LocalDateTime.now());
                interview.setCancellationReason(request.getCancellationReason());
            }
        }
        if (request.getFeedback() != null) {
            interview.setFeedback(request.getFeedback());
        }
        if (request.getRating() != null) {
            interview.setRating(request.getRating());
        }
        if (request.getOutcome() != null) {
            interview.setOutcome(request.getOutcome());
        }
        if (request.getCancellationReason() != null) {
            interview.setCancellationReason(request.getCancellationReason());
        }

        interview.setUpdatedAt(LocalDateTime.now());

        interview = interviewRepository.save(interview);

        // Send notification event
        sendInterviewEvent("interview.updated", interview);

        log.info("Updated interview with ID: {}", id);

        return mapToResponse(interview);
    }

    @Transactional
    public void cancelInterview(Long id, String reason) {
        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> new InterviewNotFoundException("Interview not found with ID: " + id));

        if (interview.getStatus() == InterviewStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot cancel completed interview");
        }

        interview.setStatus(InterviewStatus.CANCELLED);
        interview.setCancelledAt(LocalDateTime.now());
        interview.setCancellationReason(reason);
        interview.setUpdatedAt(LocalDateTime.now());

        interviewRepository.save(interview);

        // Send notification event
        sendInterviewEvent("interview.cancelled", interview);

        log.info("Cancelled interview with ID: {}. Reason: {}", id, reason);
    }

    public InterviewResponse getInterviewById(Long id) {
        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> new InterviewNotFoundException("Interview not found with ID: " + id));

        return mapToResponse(interview);
    }

    public InterviewResponse getInterviewByApplicationId(Long applicationId) {
        Interview interview = interviewRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new InterviewNotFoundException("No interview found for application ID: " + applicationId));

        return mapToResponse(interview);
    }

    public Page<InterviewResponse> getInterviewsByRecruiter(Long recruiterId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "scheduledAt"));
        Page<Interview> interviews = interviewRepository.findByRecruiterId(recruiterId, pageable);

        return interviews.map(this::mapToResponse);
    }

    public Page<InterviewResponse> getInterviewsByCandidate(Long candidateId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "scheduledAt"));
        Page<Interview> interviews = interviewRepository.findByCandidateId(candidateId, pageable);

        return interviews.map(this::mapToResponse);
    }

    public Page<InterviewResponse> getInterviewsByStatus(InterviewStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "scheduledAt"));
        Page<Interview> interviews = interviewRepository.findByStatus(status, pageable);

        return interviews.map(this::mapToResponse);
    }

    public List<InterviewResponse> getUpcomingInterviews() {
        List<Interview> interviews = interviewRepository.findUpcomingInterviews(
                LocalDateTime.now(), 
                InterviewStatus.SCHEDULED
        );
        return interviews.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<InterviewResponse> getOverdueInterviews() {
        List<Interview> interviews = interviewRepository.findOverdueInterviews(
                LocalDateTime.now(),
                InterviewStatus.COMPLETED,
                InterviewStatus.CANCELLED
        );
        return interviews.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<InterviewResponse> getInterviewsByUserId(Long userId) {
        // Get interviews where user is either recruiter or candidate
        List<Interview> recruiterInterviews = interviewRepository.findByRecruiterIdAndStatusIn(
                userId, 
                List.of(InterviewStatus.SCHEDULED, InterviewStatus.CONFIRMED, InterviewStatus.RESCHEDULED)
        );
        
        List<Interview> candidateInterviews = interviewRepository.findByCandidateIdAndStatusIn(
                userId, 
                List.of(InterviewStatus.SCHEDULED, InterviewStatus.CONFIRMED, InterviewStatus.RESCHEDULED)
        );

        List<Interview> allInterviews = recruiterInterviews;
        allInterviews.addAll(candidateInterviews);

        return allInterviews.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public long getInterviewsCountByRecruiterAndStatus(Long recruiterId, InterviewStatus status) {
        return interviewRepository.countByRecruiterIdAndStatus(recruiterId, status);
    }

    public long getInterviewsCountByCandidateAndStatus(Long candidateId, InterviewStatus status) {
        return interviewRepository.countByCandidateIdAndStatus(candidateId, status);
    }

    public Page<InterviewResponse> getAllInterviews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "scheduledAt"));
        Page<Interview> interviews = interviewRepository.findAll(pageable);
        
        return interviews.map(this::mapToResponse);
    }

    private void sendInterviewEvent(String eventType, Interview interview) {
        try {
            rabbitTemplate.convertAndSend("notification.exchange", eventType, interview);
            log.info("Sent {} event for interview {}", eventType, interview.getId());
        } catch (Exception e) {
            log.error("Failed to send {} event for interview {}", eventType, interview.getId(), e);
        }
    }

    private InterviewResponse mapToResponse(Interview interview) {
        return new InterviewResponse(
                interview.getId(),
                interview.getApplicationId(),
                interview.getRecruiterId(),
                interview.getCandidateId(),
                interview.getScheduledAt(),
                interview.getMode(),
                interview.getMeetingLink(),
                interview.getLocation(),
                interview.getNotes(),
                interview.getStatus(),
                interview.getCreatedAt(),
                interview.getUpdatedAt(),
                interview.getConfirmedAt(),
                interview.getRescheduledAt(),
                interview.getCompletedAt(),
                interview.getCancelledAt(),
                interview.getCancellationReason(),
                interview.getDuration(),
                interview.getInterviewType(),
                interview.getInterviewerName(),
                interview.getFeedback(),
                interview.getRating(),
                interview.getOutcome(),
                interview.getCandidateName(),
                interview.getJobTitle()
        );
    }
}
