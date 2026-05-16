package com.hireconnect.job.dto;

import com.hireconnect.job.entity.JobType;
import lombok.Data;

import java.util.List;

@Data
public class JobSearchRequest {
    
    private String keyword;
    
    private String location;
    
    private JobType type;
    
    private List<String> skills;
    
    private String category;
    
    private Boolean isRemote;
    
    private String workMode;
    
    private Integer minExperience;
    
    private Integer maxExperience;
    
    private Integer minSalary;
    
    private Integer maxSalary;
    
    private String sortBy = "createdAt"; // createdAt, salary, title
    
    private String sortDirection = "desc"; // asc, desc
    
    private Integer page = 0;
    
    private Integer size = 10;
}
