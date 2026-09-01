package com.project.taskmanager.exception;

public class ProjectNotEmptyException extends RuntimeException {
    public ProjectNotEmptyException(String message) {
        super(message);
    }
}