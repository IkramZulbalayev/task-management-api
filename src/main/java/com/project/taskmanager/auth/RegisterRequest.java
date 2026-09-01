package com.project.taskmanager.auth;

public class RegisterRequest {

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String organizationName;

    public RegisterRequest() {
    }

    public RegisterRequest(String firstName, String lastName, String email, String password, String organizationName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.organizationName = organizationName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getOrganizationName() {
        return organizationName;
    }
}