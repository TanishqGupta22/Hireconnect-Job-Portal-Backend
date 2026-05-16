package com.hireconnect.interview.repository;

import com.hireconnect.interview.entity.Interview;
import com.hireconnect.interview.entity.InterviewMode;
import com.hireconnect.interview.entity.InterviewStatus;
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
public interface InterviewRepository extends JpaRepository<Interview, Long> {
    
    Optional<Interview> findByApplicationId(Long applicationId);
    
    Page<Interview> findByRecruiterId(Long recruiterId, Pageable pageable);
    
    Page<Interview> findByCandidateId(Long candidateId, Pageable pageable);
    
    Page<Interview> findByStatus(InterviewStatus status, Pageable pageable);
    
    Page<Interview> findByRecruiterIdAndStatus(Long recruiterId, InterviewStatus status, Pageable pageable);
    
    Page<Interview> findByCandidateIdAndStatus(Long candidateId, InterviewStatus status, Pageable pageable);
    
    @Query("SELECT i FROM Interview i WHERE i.scheduledAt >= :startDate AND i.scheduledAt <= :endDate")
    List<Interview> findByScheduledAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT i FROM Interview i WHERE i.candidateId = :candidateId AND i.status IN :statuses")
    List<Interview> findByCandidateIdAndStatusIn(@Param("candidateId") Long candidateId, @Param("statuses") List<InterviewStatus> statuses);
    
    @Query("SELECT i FROM Interview i WHERE i.recruiterId = :recruiterId AND i.status IN :statuses")
    List<Interview> findByRecruiterIdAndStatusIn(@Param("recruiterId") Long recruiterId, @Param("statuses") List<InterviewStatus> statuses);
    
    List<Interview> findByApplicationIdAndStatus(Long applicationId, InterviewStatus status);
    
    @Query("SELECT i FROM Interview i WHERE i.scheduledAt >= :now AND i.status = :status")
    List<Interview> findUpcomingInterviews(@Param("now") LocalDateTime now, @Param("status") InterviewStatus status);
    
    @Query("SELECT i FROM Interview i WHERE i.scheduledAt < :now AND i.status != :completedStatus AND i.status != :cancelledStatus")
    List<Interview> findOverdueInterviews(@Param("now") LocalDateTime now, @Param("completedStatus") InterviewStatus completedStatus, @Param("cancelledStatus") InterviewStatus cancelledStatus);
    
    List<Interview> findByMode(InterviewMode mode);
    
    @Query("SELECT COUNT(i) FROM Interview i WHERE i.recruiterId = :recruiterId AND i.status = :status")
    long countByRecruiterIdAndStatus(@Param("recruiterId") Long recruiterId, @Param("status") InterviewStatus status);
    
    @Query("SELECT COUNT(i) FROM Interview i WHERE i.candidateId = :candidateId AND i.status = :status")
    long countByCandidateIdAndStatus(@Param("candidateId") Long candidateId, @Param("status") InterviewStatus status);
    
    @Query("SELECT COUNT(i) FROM Interview i WHERE i.scheduledAt >= :startDate AND i.scheduledAt <= :endDate")
    long countByScheduledAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    boolean existsByApplicationId(Long applicationId);
}
