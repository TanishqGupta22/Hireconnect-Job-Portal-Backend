package com.hireconnect.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RecruiterProfileRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 100, message = "Company name must be between 2 and 100 characters")
    private String companyName;
    
    private String website;
    
    private String industry;
    
    private String companySize;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    private String logoUrl;
    
    private String address;
    
    private String linkedin;
    
    private String contactEmail;
    
    private String contactPhone;
    
    private Boolean isActive = true;
}
