package com.hireconnect.auth.service;

import com.hireconnect.auth.dto.*;
import com.hireconnect.auth.entity.AuthProvider;
import com.hireconnect.auth.entity.RefreshToken;
import com.hireconnect.auth.entity.Role;
import com.hireconnect.auth.entity.User;
import com.hireconnect.auth.exception.BadRequestException;
import com.hireconnect.auth.exception.UnauthorizedException;
import com.hireconnect.auth.repository.RefreshTokenRepository;
import com.hireconnect.auth.repository.UserRepository;
import com.hireconnect.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @Value("${app.jwt.refresh-token-expiry-ms}")
    private long refreshTokenExpiryMs;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Starting registration for email: {}", request.getEmail());
        
        try {
            // Check if user already exists
            if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
                log.warn("Registration failed: Email already exists - {}", request.getEmail());
                throw new BadRequestException("Email already exists");
            }

            // Create new user
            User user = User.builder()
                    .name(request.getName())
                    .email(request.getEmail().toLowerCase())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(request.getRole())
                    .provider(AuthProvider.LOCAL)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            user = userRepository.save(user);
            log.info("User saved successfully: {}", user.getEmail());

            // Send registration event to RabbitMQ IMMEDIATELY after save
            try {
                java.util.Map<String, Object> event = new java.util.HashMap<>();
                event.put("userId", user.getId());
                event.put("email", user.getEmail());
                event.put("name", user.getName());
                
                System.out.println("BEFORE RABBITMQ");
                rabbitTemplate.convertAndSend("user.registered.queue", event);
                System.out.println("AFTER RABBITMQ");
                
                System.out.println("RabbitMQ event sent successfully");
                log.info("Sent user.registered event for user: {}", user.getEmail());
            } catch (Exception e) {
                log.error("Failed to send user.registered event", e);
            }
            
            // Generate tokens
            String accessToken = jwtService.generateToken(user.getEmail());
            String refreshToken = jwtService.generateRefreshToken(user.getEmail());
            
            // Save refresh token
            saveRefreshToken(user, refreshToken);
            
            log.info("Registration completed successfully for: {}", user.getEmail());
            
            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(3600)
                    .user(mapToUserResponse(user))
                    .build();
                    
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Registration failed for email: {} - Error: {}", request.getEmail(), e.getMessage(), e);
            throw new BadRequestException("Registration failed: " + e.getMessage());
        }
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase();
        log.info("Processing login request for email: {}", email);
        
        try {
            log.debug("Step 1: Starting authentication for email: {}", email);
            
            // Validate input
            if (request.getEmail() == null || request.getPassword() == null) {
                log.error("Null input detected - email: {}, password: {}", request.getEmail(), request.getPassword());
                throw new BadRequestException("Email and password are required");
            }
            
            log.debug("Step 2: Authenticating user with authenticationManager");
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            request.getPassword()
                    )
            );
            log.info("Step 3: Authentication successful for email: {}", email);

            log.debug("Step 4: Fetching user from database");
            // Get the original User entity from database
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
            
            log.info("Step 5: User found - ID: {}, Email: {}", user.getId(), user.getEmail());
            
            log.debug("Step 6: Updating last login time");
            // Update last login time
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
            log.info("Step 7: Updated last login time for user: {}", user.getId());

            log.debug("Step 8: Issuing tokens");
            // Issue tokens using consistent method
            AuthResponse response = issueTokens(user);
            log.info("Step 9: Tokens issued successfully for user: {}", user.getId());
            
            log.info("Login completed successfully for user: {}", user.getId());
            return response;
            
        } catch (Exception e) {
            log.error("Login failed for email: {} - Error type: {}, Message: {}", 
                     email, e.getClass().getSimpleName(), e.getMessage(), e);
            if (e instanceof org.springframework.security.authentication.BadCredentialsException) {
                throw new UnauthorizedException("Invalid credentials");
            }
            throw e;
        }
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (refreshToken.isExpired() || refreshToken.getRevoked()) {
            refreshTokenRepository.delete(refreshToken);
            throw new UnauthorizedException("Refresh token is expired or revoked");
        }

        User user = refreshToken.getUser();
        refreshTokenRepository.delete(refreshToken);

        return issueTokens(user);
    }

    @Transactional
    public void logout(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));
        
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new BadRequestException("User not found with this email"));

        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
        log.info("Password reset token sent to email: {}", user.getEmail());

        // Send password reset event to RabbitMQ
        try {
            java.util.Map<String, Object> event = new java.util.HashMap<>();
            event.put("id", user.getId());
            event.put("email", user.getEmail());
            event.put("resetToken", resetToken);
            
            rabbitTemplate.convertAndSend("notification.exchange", "user.password.reset", event);
            log.info("Sent user.password.reset event for user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send user.password.reset event", e);
        }
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid reset token"));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        log.info("Password reset successful for user: {}", user.getEmail());
    }

    public boolean validateToken(String token) {
        try {
            String username = jwtService.extractUsername(token);
            return jwtService.isTokenValid(token, username);
        } catch (Exception e) {
            return false;
        }
    }

    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        
        log.info("Retrieved current user: {}", user.getId());
        return mapToUserResponse(user);
    }

    @Transactional
    public AuthResponse issueTokens(User user) {
        log.debug("issueTokens() started for user ID: {}", user != null ? user.getId() : "null");
        
        try {
            // Validate user input
            if (user == null) {
                log.error("User is null in issueTokens()");
                throw new BadRequestException("User cannot be null");
            }
            
            if (user.getEmail() == null) {
                log.error("User email is null for user ID: {}", user.getId());
                throw new BadRequestException("User email cannot be null");
            }
            
            log.debug("Step 1: Revoking existing refresh tokens for user: {}", user.getId());
            // Revoke all existing refresh tokens for this user
            refreshTokenRepository.deleteByUser(user);

            log.debug("Step 2: Generating access token for email: {}", user.getEmail());
            java.util.Map<String, Object> claims = new java.util.HashMap<>();
            claims.put("id", user.getId());
            claims.put("role", user.getRole().name());
            claims.put("name", user.getName());
            
            String accessToken = jwtService.generateToken(claims, user.getEmail());
            if (accessToken == null) {
                log.error("Access token generation returned null for user: {}", user.getId());
                throw new BadRequestException("Failed to generate access token");
            }
            log.debug("Step 3: Access token generated successfully (length: {})", accessToken.length());

            log.debug("Step 4: Generating refresh token for user: {}", user.getId());
            String refreshToken = generateRefreshToken(user);
            if (refreshToken == null) {
                log.error("Refresh token generation returned null for user: {}", user.getId());
                throw new BadRequestException("Failed to generate refresh token");
            }
            log.debug("Step 5: Refresh token generated successfully (length: {})", refreshToken.length());

            log.debug("Step 6: Mapping user to response DTO");
            UserResponse userResponse = mapToUserResponse(user);
            if (userResponse == null) {
                log.error("UserResponse mapping returned null for user: {}", user.getId());
                throw new BadRequestException("Failed to map user response");
            }
            log.debug("Step 7: UserResponse mapped successfully");

            log.debug("Step 8: Getting token expiry seconds");
            Long expirySeconds = jwtService.getAccessTokenExpirySeconds();
            if (expirySeconds == null) {
                log.error("Token expiry seconds returned null");
                expirySeconds = 3600L; // fallback
                log.warn("Using fallback expiry seconds: {}", expirySeconds);
            }
            log.debug("Step 9: Token expiry seconds: {}", expirySeconds);

            log.debug("Step 10: Building AuthResponse");
            AuthResponse response = AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(expirySeconds)
                    .user(userResponse)
                    .build();
            
            if (response == null) {
                log.error("AuthResponse builder returned null for user: {}", user.getId());
                throw new BadRequestException("Failed to build auth response");
            }
            
            log.debug("issueTokens() completed successfully for user: {}", user.getId());
            return response;
            
        } catch (Exception e) {
            log.error("issueTokens() failed for user ID: {} - Error type: {}, Message: {}", 
                     user != null ? user.getId() : "null", e.getClass().getSimpleName(), e.getMessage(), e);
            throw e;
        }
    }

    private void saveRefreshToken(User user, String refreshToken) {
        RefreshToken token = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenExpiryMs / 1000))
                .createdAt(LocalDateTime.now())
                .revoked(false)
                .build();
        refreshTokenRepository.save(token);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .status(user.getStatus())
                .lastLoginAt(user.getLastLoginAt())
                .emailVerified(user.getEmailVerified())
                .companyName(fetchCompanyName(user))
                .build();
    }

    private String fetchCompanyName(User user) {
        if (user.getRole() != Role.RECRUITER) {
            return null;
        }
        try {
            return jdbcTemplate.queryForObject(
                "SELECT company_name FROM hireconnect_profiles.recruiter_profiles WHERE user_id = ?", 
                String.class, user.getId());
        } catch (Exception e) {
            return "Not Set";
        }
    }

    private String generateRefreshToken(User user) {
        log.debug("generateRefreshToken() started for user ID: {}", user != null ? user.getId() : "null");
        
        try {
            if (user == null) {
                log.error("User is null in generateRefreshToken()");
                throw new BadRequestException("User cannot be null");
            }
            
            log.debug("Creating refresh token entity for user: {}", user.getId());
            RefreshToken refreshToken = RefreshToken.builder()
                    .token(UUID.randomUUID().toString())
                    .user(user)
                    .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenExpiryMs / 1000))
                    .createdAt(LocalDateTime.now())
                    .revoked(false)
                    .build();

            log.debug("Saving refresh token to database");
            refreshToken = refreshTokenRepository.save(refreshToken);
            
            String token = refreshToken.getToken();
            log.debug("generateRefreshToken() completed successfully for user: {}, token length: {}", 
                     user.getId(), token != null ? token.length() : "null");
            return token;
            
        } catch (Exception e) {
            log.error("generateRefreshToken() failed for user ID: {} - Error type: {}, Message: {}", 
                     user != null ? user.getId() : "null", e.getClass().getSimpleName(), e.getMessage(), e);
            throw e;
        }
    }

    public java.util.List<UserResponse> getAllUsers(Role role) {
        java.util.List<User> users;
        if (role != null) {
            log.info("Fetching users for role: {}", role);
            users = userRepository.findByRole(role);
        } else {
            log.info("Fetching all users by combining roles");
            users = new java.util.ArrayList<>();
            users.addAll(userRepository.findByRole(Role.ADMIN));
            users.addAll(userRepository.findByRole(Role.RECRUITER));
            users.addAll(userRepository.findByRole(Role.CANDIDATE));
            
            // Fallback if roles are empty
            if (users.isEmpty()) {
                log.info("Role-based fetch empty, falling back to findAll()");
                users = userRepository.findAll();
            }
        }
        log.info("Total users found: {}", users.size());
        return users.stream().map(this::mapToUserResponse).collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public void updateUserStatus(Long userId, com.hireconnect.auth.entity.UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
        user.setStatus(status);
        userRepository.save(user);
        log.info("Updated status of user {} to {}", userId, status);
    }
}
