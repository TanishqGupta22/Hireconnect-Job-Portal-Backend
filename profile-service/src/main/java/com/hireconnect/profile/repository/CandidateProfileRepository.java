package com.hireconnect.profile.repository;

import com.hireconnect.profile.entity.CandidateProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateProfileRepository extends JpaRepository<CandidateProfile, Long> {
    
    Optional<CandidateProfile> findByUserId(Long userId);
    
    List<CandidateProfile> findByIsPublicTrue();
    
    List<CandidateProfile> findByIsAvailableTrueAndIsPublicTrue();
    
    boolean existsByUserId(Long userId);
    
    void deleteByUserId(Long userId);
}
