package com.hireconnect.application.repository;

import com.hireconnect.application.entity.Application;
import com.hireconnect.application.entity.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    
    Optional<Application> findByJobIdAndCandidateId(Long jobId, Long candidateId);
    
    Page<Application> findByCandidateId(Long candidateId, Pageable pageable);
    
    Page<Application> findByJobId(Long jobId, Pageable pageable);
    
    Page<Application> findByStatus(ApplicationStatus status, Pageable pageable);
    
    Page<Application> findByCandidateIdAndStatus(Long candidateId, ApplicationStatus status, Pageable pageable);
    
    Page<Application> findByJobIdAndStatus(Long jobId, ApplicationStatus status, Pageable pageable);
    
    Page<Application> findByRecruiterId(Long recruiterId, Pageable pageable);
    
    Page<Application> findByRecruiterIdAndStatus(Long recruiterId, ApplicationStatus status, Pageable pageable);
    
    long countByRecruiterId(Long recruiterId);
    
    long countByRecruiterIdAndStatus(Long recruiterId, ApplicationStatus status);
    
    Page<Application> findByJobIdAndStatusNot(Long jobId, ApplicationStatus status, Pageable pageable);
    
    Page<Application> findByCandidateIdAndStatusNot(Long candidateId, ApplicationStatus status, Pageable pageable);
    
    List<Application> findByJobIdAndIsShortlistedTrue(Long jobId);
    
    List<Application> findByJobIdAndStatus(Long jobId, ApplicationStatus status);
    
    List<Application> findByCandidateIdAndStatus(Long candidateId, ApplicationStatus status);
    
    long countByJobIdAndStatus(Long jobId, ApplicationStatus status);
    
    long countByJobId(Long jobId);
    
    long countByCandidateId(Long candidateId);
    
    List<Application> findByAppliedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Application> findByStatusAndAppliedAtBetween(ApplicationStatus status, LocalDateTime startDate, LocalDateTime endDate);
    
    boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId);
}
