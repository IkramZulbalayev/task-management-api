package com.project.taskmanager.project;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProjectResponse {

    private Long id;

    private String name;

    private String description;

    private LocalDateTime createdAt;

    private String createdByName;

    public ProjectResponse(){

    }

    public ProjectResponse(Long id, String name, String description, LocalDateTime createdAt, String createdByName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.createdByName = createdByName;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getCreatedByName() {
        return createdByName;
    }
}
