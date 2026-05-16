package com.hireconnect.application.service;

import com.hireconnect.application.dto.ApplicationRequest;
import com.hireconnect.application.dto.ApplicationResponse;
import com.hireconnect.application.dto.ApplicationStatusUpdateRequest;
import com.hireconnect.application.entity.Application;
import com.hireconnect.application.entity.ApplicationStatus;
import com.hireconnect.application.exception.ApplicationNotFoundException;
import com.hireconnect.application.exception.DuplicateApplicationException;
import com.hireconnect.application.repository.ApplicationRepository;
import com.hireconnect.application.client.SubscriptionClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private SubscriptionClient subscriptionClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ApplicationService applicationService;

    private Application application;
    private ApplicationRequest applicationRequest;

    @BeforeEach
    void setUp() {
        application = Application.builder()
                .id(1L)
                .jobId(101L)
                .candidateId(201L)
                .recruiterId(301L)
                .status(ApplicationStatus.APPLIED)
                .build();

        applicationRequest = new ApplicationRequest();
        applicationRequest.setJobId(101L);
        applicationRequest.setCandidateId(201L);
        applicationRequest.setRecruiterId(301L);
    }

    @Test
    void applyForJob_Success() {
        when(subscriptionClient.checkLimit(anyLong(), anyString())).thenReturn(true);
        when(applicationRepository.existsByJobIdAndCandidateId(anyLong(), anyLong())).thenReturn(false);
        when(applicationRepository.save(any(Application.class))).thenReturn(application);

        ApplicationResponse response = applicationService.applyForJob(applicationRequest);

        assertNotNull(response);
        assertEquals(ApplicationStatus.APPLIED, response.getStatus());
        verify(applicationRepository, times(1)).save(any(Application.class));
    }

    @Test
    void applyForJob_Duplicate_ThrowsException() {
        when(subscriptionClient.checkLimit(anyLong(), anyString())).thenReturn(true);
        when(applicationRepository.existsByJobIdAndCandidateId(anyLong(), anyLong())).thenReturn(true);

        assertThrows(DuplicateApplicationException.class, () -> applicationService.applyForJob(applicationRequest));
    }

    @Test
    void withdrawApplication_Success() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

        applicationService.withdrawApplication(1L, 201L);

        assertEquals(ApplicationStatus.WITHDRAWN, application.getStatus());
        verify(applicationRepository, times(1)).save(application);
    }

    @Test
    void withdrawApplication_NotFound_ThrowsException() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class, () -> applicationService.withdrawApplication(1L, 201L));
    }

    @Test
    void updateApplicationStatus_Success() {
        ApplicationStatusUpdateRequest updateRequest = new ApplicationStatusUpdateRequest();
        updateRequest.setStatus(ApplicationStatus.SHORTLISTED);
        
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(applicationRepository.save(any(Application.class))).thenReturn(application);

        ApplicationResponse response = applicationService.updateApplicationStatus(1L, updateRequest);

        assertNotNull(response);
        assertEquals(ApplicationStatus.SHORTLISTED, response.getStatus());
    }
}
