package com.project.taskmanager.auth;

public class AuthResponse {

    private String token;
    private String email;
    private String role;
    private String firstName;
    private String lastName;
    private String organizationName;

    public AuthResponse() {
    }

    public AuthResponse(String token, String email, String role,
                        String firstName, String lastName, String organizationName) {
        this.token = token;
        this.email = email;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
        this.organizationName = organizationName;
    }

    public String getToken() { return token; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getOrganizationName() { return organizationName; }
}