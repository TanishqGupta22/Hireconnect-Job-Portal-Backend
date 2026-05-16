package com.hireconnect.profile.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CandidateProfileRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;
    
    private String headline;
    
    @NotBlank(message = "Mobile number is required")
    private String mobile;
    
    private List<String> skills;
    
    private List<String> experience;
    
    private List<String> education;
    
    private String resumeUrl;
    
    private String profileImageUrl;
    
    private String address;
    
    private String linkedin;
    
    private String github;
    
    private String portfolio;
    
    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;
    
    private Boolean isPublic = true;
    
    private Boolean isAvailable = true;
    
    private Boolean jobAlertsEnabled = false;
}
