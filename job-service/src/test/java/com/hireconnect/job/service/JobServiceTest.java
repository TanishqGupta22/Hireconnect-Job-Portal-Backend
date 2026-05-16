package com.hireconnect.job.service;

import com.hireconnect.job.dto.JobRequest;
import com.hireconnect.job.dto.JobResponse;
import com.hireconnect.job.entity.Job;
import com.hireconnect.job.entity.JobStatus;
import com.hireconnect.job.entity.JobType;
import com.hireconnect.job.exception.JobNotFoundException;
import com.hireconnect.job.repository.JobRepository;
import com.hireconnect.job.client.SubscriptionClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private SubscriptionClient subscriptionClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private JobService jobService;

    private Job job;
    private JobRequest jobRequest;

    @BeforeEach
    void setUp() {
        job = Job.builder()
                .id(1L)
                .recruiterId(101L)
                .title("Software Engineer")
                .description("Job Description")
                .status(JobStatus.ACTIVE)
                .salaryMin(new BigDecimal("50000"))
                .salaryMax(new BigDecimal("80000"))
                .build();

        jobRequest = new JobRequest();
        jobRequest.setRecruiterId(101L);
        jobRequest.setTitle("Software Engineer");
        jobRequest.setDescription("Job Description");
        jobRequest.setSalaryMin(new BigDecimal("50000"));
        jobRequest.setSalaryMax(new BigDecimal("80000"));
    }

    @Test
    void createJob_Success() {
        when(subscriptionClient.checkLimit(anyLong(), anyString())).thenReturn(true);
        when(jobRepository.save(any(Job.class))).thenReturn(job);

        JobResponse response = jobService.createJob(jobRequest);

        assertNotNull(response);
        assertEquals(job.getTitle(), response.getTitle());
        verify(jobRepository, times(1)).save(any(Job.class));
        verify(subscriptionClient, times(1)).incrementUsage(anyLong(), anyString());
    }

    @Test
    void createJob_LimitReached_ThrowsException() {
        when(subscriptionClient.checkLimit(anyLong(), anyString())).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> jobService.createJob(jobRequest));
        verify(jobRepository, never()).save(any(Job.class));
    }

    @Test
    void getJobById_Success() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        JobResponse response = jobService.getJobById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void getJobById_NotFound_ThrowsException() {
        when(jobRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(JobNotFoundException.class, () -> jobService.getJobById(1L));
    }

    @Test
    void deleteJob_Success() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        jobService.deleteJob(1L, 101L);

        verify(jobRepository, times(1)).delete(job);
    }

    @Test
    void deleteJob_Unauthorized_ThrowsException() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        assertThrows(IllegalArgumentException.class, () -> jobService.deleteJob(1L, 999L));
        verify(jobRepository, never()).delete(any(Job.class));
    }
}
