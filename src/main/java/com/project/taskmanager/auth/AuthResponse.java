package com.project.taskmanager.auth;

public class AuthResponse {

    private String token;
    private String email;
    private String role;

    public AuthResponse() {
    }

    public AuthResponse(String email, String role, String token) {
        this.email = email;
        this.role = role;
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getToken() {
        return token;
    }
}