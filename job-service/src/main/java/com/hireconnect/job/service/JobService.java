package com.hireconnect.job.service;

import com.hireconnect.job.dto.JobRequest;
import com.hireconnect.job.dto.JobResponse;
import com.hireconnect.job.dto.JobSearchRequest;
import com.hireconnect.job.entity.Job;
import com.hireconnect.job.entity.JobStatus;
import com.hireconnect.job.entity.JobType;
import com.hireconnect.job.exception.JobNotFoundException;
import com.hireconnect.job.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {

    private final JobRepository jobRepository;
    private final com.hireconnect.job.client.SubscriptionClient subscriptionClient;
    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Transactional
    /*@Caching(evict = {
        @CacheEvict(value = "jobs", allEntries = true),
        @CacheEvict(value = "activeJobs", allEntries = true)
    })*/
    public JobResponse createJob(JobRequest request) {
        // Check subscription limit
        Boolean canPost = subscriptionClient.checkLimit(request.getRecruiterId(), "RECRUITER");
        if (Boolean.FALSE.equals(canPost)) {
            throw new IllegalStateException("You have reached your job posting limit. Please upgrade your subscription.");
        }

        Job job = Job.builder()
                .recruiterId(request.getRecruiterId())
                .title(request.getTitle())
                .description(request.getDescription())
                .skills(request.getSkills())
                .category(request.getCategory())
                .type(request.getType())
                .location(request.getLocation())
                .workMode(request.getWorkMode())
                .salaryMin(request.getSalaryMin())
                .salaryMax(request.getSalaryMax())
                .salaryCurrency(request.getSalaryCurrency())
                .experienceRequired(request.getExperienceRequired())
                .educationLevel(request.getEducationLevel())
                .status(JobStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .expiresAt(request.getExpiresAt())
                .isFeatured(request.getIsFeatured())
                .isRemote(request.getIsRemote())
                .applicationDeadline(request.getApplicationDeadline())
                .vacancyCount(request.getVacancyCount())
                .department(request.getDepartment())
                .benefits(request.getBenefits())
                .requirements(request.getRequirements())
                .responsibilities(request.getResponsibilities())
                .build();

        job = jobRepository.save(job);
        log.info("Created job with ID: {} for recruiter: {}", job.getId(), job.getRecruiterId());

        // Increment usage
        subscriptionClient.incrementUsage(job.getRecruiterId(), "RECRUITER");

        // Emit job created event for alerts
        sendJobEvent("job.created", job);

        return mapToResponse(job);
    }

    private void sendJobEvent(String eventType, Job job) {
        try {
            rabbitTemplate.convertAndSend("notification.exchange", eventType, job);
            log.info("Sent {} event for job {}", eventType, job.getId());
        } catch (Exception e) {
            log.error("Failed to send {} event for job {}", eventType, job.getId(), e);
        }
    }

    //@Cacheable(value = "jobs", key = "#id")
    public JobResponse getJobById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException("Job not found with ID: " + id));
        
        return mapToResponse(job);
    }

    @Transactional
    /*@Caching(evict = {
        @CacheEvict(value = "jobs", key = "#id"),
        @CacheEvict(value = "activeJobs", allEntries = true)
    })*/
    public JobResponse updateJob(Long id, JobRequest request) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException("Job not found with ID: " + id));

        if (!job.getRecruiterId().equals(request.getRecruiterId())) {
            throw new IllegalArgumentException("You can only update your own jobs");
        }

        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setSkills(request.getSkills());
        job.setCategory(request.getCategory());
        job.setType(request.getType());
        job.setLocation(request.getLocation());
        job.setWorkMode(request.getWorkMode());
        job.setSalaryMin(request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax());
        job.setSalaryCurrency(request.getSalaryCurrency());
        job.setExperienceRequired(request.getExperienceRequired());
        job.setEducationLevel(request.getEducationLevel());
        job.setExpiresAt(request.getExpiresAt());
        job.setIsFeatured(request.getIsFeatured());
        job.setIsRemote(request.getIsRemote());
        job.setApplicationDeadline(request.getApplicationDeadline());
        job.setVacancyCount(request.getVacancyCount());
        job.setDepartment(request.getDepartment());
        job.setBenefits(request.getBenefits());
        job.setRequirements(request.getRequirements());
        job.setResponsibilities(request.getResponsibilities());
        job.setUpdatedAt(LocalDateTime.now());

        job = jobRepository.save(job);
        log.info("Updated job with ID: {}", id);

        return mapToResponse(job);
    }

    @Transactional
    /*@Caching(evict = {
        @CacheEvict(value = "jobs", key = "#id"),
        @CacheEvict(value = "activeJobs", allEntries = true)
    })*/
    public void deleteJob(Long id, Long recruiterId) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException("Job not found with ID: " + id));

        if (!job.getRecruiterId().equals(recruiterId)) {
            throw new IllegalArgumentException("You can only delete your own jobs");
        }

        jobRepository.delete(job);
        log.info("Deleted job with ID: {}", id);
    }

    @Transactional
    /*@Caching(evict = {
        @CacheEvict(value = "jobs", key = "#id"),
        @CacheEvict(value = "activeJobs", allEntries = true)
    })*/
    public JobResponse updateJobStatus(Long id, JobStatus status, Long recruiterId) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException("Job not found with ID: " + id));

        if (!job.getRecruiterId().equals(recruiterId)) {
            throw new IllegalArgumentException("You can only update your own jobs");
        }

        job.setStatus(status);
        job.setUpdatedAt(LocalDateTime.now());

        job = jobRepository.save(job);
        log.info("Updated job status to {} for job ID: {}", status, id);

        return mapToResponse(job);
    }

    //@Cacheable(value = "activeJobs", key = "#page + '-' + #size")
    public Page<JobResponse> getAllActiveJobs(int page, int size) {
        log.info("Fetching all posted jobs, page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Job> jobs = jobRepository.findAll(pageable);
        return jobs.map(this::mapToResponse);
    }

    public Page<JobResponse> getJobsByRecruiter(Long recruiterId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Job> jobs = jobRepository.findByRecruiterId(recruiterId, pageable);
        
        return jobs.map(this::mapToResponse);
    }

    public Page<JobResponse> searchJobs(JobSearchRequest searchRequest) {
        // Create sort and page
        Sort.Direction direction = searchRequest.getSortDirection().equalsIgnoreCase("asc") 
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(
                searchRequest.getPage(), 
                searchRequest.getSize(), 
                Sort.by(direction, searchRequest.getSortBy())
        );

        Page<Job> jobs;

        // Apply search filters
        if (searchRequest.getKeyword() != null && !searchRequest.getKeyword().trim().isEmpty()) {
            jobs = jobRepository.findByStatusAndKeyword(JobStatus.ACTIVE, searchRequest.getKeyword(), pageable);
        } else if (searchRequest.getLocation() != null && !searchRequest.getLocation().trim().isEmpty()) {
            jobs = jobRepository.findByStatusAndLocation(JobStatus.ACTIVE, searchRequest.getLocation(), pageable);
        } else if (searchRequest.getType() != null) {
            jobs = jobRepository.findByStatusAndType(JobStatus.ACTIVE, searchRequest.getType(), pageable);
        } else if (searchRequest.getSkills() != null && !searchRequest.getSkills().isEmpty()) {
            jobs = jobRepository.findByStatusAndSkills(JobStatus.ACTIVE, searchRequest.getSkills(), pageable);
        } else if (searchRequest.getCategory() != null && !searchRequest.getCategory().trim().isEmpty()) {
            jobs = jobRepository.findByStatusAndCategory(JobStatus.ACTIVE, searchRequest.getCategory(), pageable);
        } else if (Boolean.TRUE.equals(searchRequest.getIsRemote())) {
            jobs = jobRepository.findRemoteJobs(JobStatus.ACTIVE, pageable);
        } else if (searchRequest.getMinSalary() != null && searchRequest.getMaxSalary() != null) {
            jobs = jobRepository.findByStatusAndSalaryRange(
                    JobStatus.ACTIVE, 
                    BigDecimal.valueOf(searchRequest.getMinSalary()), 
                    BigDecimal.valueOf(searchRequest.getMaxSalary()), 
                    pageable
            );
        } else if (searchRequest.getMinExperience() != null && searchRequest.getMaxExperience() != null) {
            jobs = jobRepository.findByStatusAndExperienceRange(
                    JobStatus.ACTIVE, 
                    searchRequest.getMinExperience(), 
                    searchRequest.getMaxExperience(), 
                    pageable
            );
        } else if (Boolean.TRUE.equals(searchRequest.getIsRemote())) {
            jobs = jobRepository.findRemoteJobs(JobStatus.ACTIVE, pageable);
        } else {
            jobs = jobRepository.findByStatus(JobStatus.ACTIVE, pageable);
        }

        return jobs.map(this::mapToResponse);
    }

    public Page<JobResponse> getFeaturedJobs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Job> jobs = jobRepository.findFeaturedJobs(JobStatus.ACTIVE, pageable);
        
        return jobs.map(this::mapToResponse);
    }

    public Page<JobResponse> getRemoteJobs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Job> jobs = jobRepository.findByStatusAndIsRemoteTrue(JobStatus.ACTIVE, pageable);
        
        return jobs.map(this::mapToResponse);
    }

    public List<JobResponse> getJobsByType(JobType type) {
        List<Job> jobs = jobRepository.findByStatusAndType(JobStatus.ACTIVE, type, Pageable.unpaged()).getContent();
        return jobs.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<JobResponse> getJobsByCategory(String category) {
        List<Job> jobs = jobRepository.findByStatusAndCategory(JobStatus.ACTIVE, category, Pageable.unpaged()).getContent();
        return jobs.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<JobResponse> getJobsBySkills(List<String> skills) {
        List<Job> jobs = jobRepository.findByStatusAndSkills(JobStatus.ACTIVE, skills, Pageable.unpaged()).getContent();
        return jobs.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public void adminDeleteJob(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException("Job not found with ID: " + id));
        jobRepository.delete(job);
        log.info("Admin deleted job with ID: {}", id);
    }

    private Long getApplicantCount(Long jobId) {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM hireconnect_applications.applications WHERE job_id = ? AND status != 'WITHDRAWN'", 
                Long.class, jobId);
        } catch (Exception e) {
            log.warn("Failed to fetch applicant count for job {}: {}", jobId, e.getMessage());
            return 0L;
        }
    }

    private JobResponse mapToResponse(Job job) {
        return new JobResponse(
                job.getId(),
                job.getRecruiterId(),
                job.getTitle(),
                job.getDescription(),
                job.getSkills(),
                job.getCategory(),
                job.getType(),
                job.getLocation(),
                job.getWorkMode(),
                job.getSalaryMin(),
                job.getSalaryMax(),
                job.getSalaryCurrency(),
                job.getExperienceRequired(),
                job.getEducationLevel(),
                job.getStatus(),
                job.getCreatedAt(),
                job.getUpdatedAt(),
                job.getExpiresAt(),
                job.getIsFeatured(),
                job.getIsRemote(),
                job.getApplicationDeadline(),
                job.getVacancyCount(),
                job.getDepartment(),
                job.getBenefits(),
                job.getRequirements(),
                job.getResponsibilities(),
                getApplicantCount(job.getId())
        );
    }
}
