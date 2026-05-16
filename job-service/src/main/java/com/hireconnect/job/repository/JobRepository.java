package com.hireconnect.job.repository;

import com.hireconnect.job.entity.Job;
import com.hireconnect.job.entity.JobStatus;
import com.hireconnect.job.entity.JobType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    
    Page<Job> findByStatus(JobStatus status, Pageable pageable);
    
    @Query("SELECT j FROM Job j WHERE j.recruiterId = :recruiterId")
    Page<Job> findByRecruiterId(@Param("recruiterId") Long recruiterId, Pageable pageable);
    
    Page<Job> findByStatusAndIsRemoteTrue(JobStatus status, Pageable pageable);
    
    @Query("SELECT j FROM Job j WHERE j.status = :status AND " +
           "(LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Job> findByStatusAndKeyword(@Param("status") JobStatus status, @Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT j FROM Job j WHERE j.status = :status AND j.location LIKE %:location%")
    Page<Job> findByStatusAndLocation(@Param("status") JobStatus status, @Param("location") String location, Pageable pageable);
    
    @Query("SELECT j FROM Job j WHERE j.status = :status AND j.type = :type")
    Page<Job> findByStatusAndType(@Param("status") JobStatus status, @Param("type") JobType type, Pageable pageable);
    
    @Query("SELECT j FROM Job j JOIN j.skills s WHERE j.status = :status AND s IN :skills")
    Page<Job> findByStatusAndSkills(@Param("status") JobStatus status, @Param("skills") List<String> skills, Pageable pageable);
    
    @Query("SELECT j FROM Job j WHERE j.status = :status AND j.category = :category")
    Page<Job> findByStatusAndCategory(@Param("status") JobStatus status, @Param("category") String category, Pageable pageable);
    
    @Query("SELECT j FROM Job j WHERE j.status = :status AND j.isRemote = true")
    Page<Job> findRemoteJobs(@Param("status") JobStatus status, Pageable pageable);
    
    @Query("SELECT j FROM Job j WHERE j.status = :status AND " +
           "j.salaryMin >= :minSalary AND j.salaryMax <= :maxSalary")
    Page<Job> findByStatusAndSalaryRange(@Param("status") JobStatus status, @Param("minSalary") java.math.BigDecimal minSalary, @Param("maxSalary") java.math.BigDecimal maxSalary, Pageable pageable);
    
    @Query("SELECT j FROM Job j WHERE j.status = :status AND " +
           "j.experienceRequired >= :minExperience AND j.experienceRequired <= :maxExperience")
    Page<Job> findByStatusAndExperienceRange(@Param("status") JobStatus status, @Param("minExperience") Integer minExperience, @Param("maxExperience") Integer maxExperience, Pageable pageable);
    
    @Query("SELECT j FROM Job j WHERE j.status = :status AND j.isFeatured = true")
    Page<Job> findFeaturedJobs(@Param("status") JobStatus status, Pageable pageable);
    
    @Query("SELECT j FROM Job j WHERE j.recruiterId = :recruiterId AND j.status = :status")
    List<Job> findByRecruiterIdAndStatus(@Param("recruiterId") Long recruiterId, @Param("status") JobStatus status);
    
    boolean existsByIdAndRecruiterId(Long id, Long recruiterId);
}
