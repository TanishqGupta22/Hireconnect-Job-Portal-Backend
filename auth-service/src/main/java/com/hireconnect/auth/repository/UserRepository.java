package com.hireconnect.auth.repository;

import com.hireconnect.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByResetToken(String resetToken);
    
    boolean existsByEmail(String email);
    
    void deleteByEmail(String email);

    java.util.List<User> findByRole(com.hireconnect.auth.entity.Role role);
    
    java.util.List<User> findByStatus(com.hireconnect.auth.entity.UserStatus status);
}
