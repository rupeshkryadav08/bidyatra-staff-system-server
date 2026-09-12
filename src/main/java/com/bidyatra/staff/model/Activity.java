package com.bidyatra.staff.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Document(collection = "activities")
public class Activity {
    @Id
    private String id;

    private LocalDate date;
    private String staffName;
    private String visitingLocation;
    private int meetDriver;
    private int login;
    private int documentsIssues;
    private String other;

    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
