package com.hireconnect.profile.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecruiterProfileResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long userId;
    private String companyName;
    private String website;
    private String industry;
    private String companySize;
    private String description;
    private String logoUrl;
    private String address;
    private String linkedin;
    private String contactEmail;
    private String contactPhone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isVerified;
    private Boolean isActive;
}
