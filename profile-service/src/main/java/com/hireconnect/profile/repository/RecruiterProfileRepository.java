package com.hireconnect.profile.repository;

import com.hireconnect.profile.entity.RecruiterProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecruiterProfileRepository extends JpaRepository<RecruiterProfile, Long> {
    
    Optional<RecruiterProfile> findByUserId(Long userId);
    
    List<RecruiterProfile> findByIsActiveTrue();
    
    List<RecruiterProfile> findByIsVerifiedTrue();
    
    boolean existsByUserId(Long userId);
    
    void deleteByUserId(Long userId);
}
