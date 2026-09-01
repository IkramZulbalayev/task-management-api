package com.project.taskmanager.comment;

import java.time.LocalDateTime;

public class CommentResponse {

    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private String authorName;

    public CommentResponse(){

    }

    public CommentResponse(Long id, String authorName, LocalDateTime createdAt, String content) {
        this.id = id;
        this.authorName = authorName;
        this.createdAt = createdAt;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getAuthorName() {
        return authorName;
    }
}
