
package com.remoteswe.crawler;

import java.time.LocalDateTime;
import java.util.List;

public class Job {
    public String jobTitle;
    public String companyName;
    public String location;
    public Double salaryMin;
    public Double salaryMax;
    public String jobType;
    public List<String> tags;
    public String url;
    public String source;
    public LocalDateTime postedDate;
    public LocalDateTime crawledAt;

    // Optional fields
    public String jobDescriptionRaw;
    public String experienceLevel;
    public String employmentTypeDetail;
    public String industry;
    public String experienceRequired;

    // Constructor
    public Job() {
        this.crawledAt = LocalDateTime.now();
    }
}
