package com.example.backend.model;

public class KeycloakUser {
    private String userId;
    private String name;
    private String email;
    private Role role;
    private String company;
    private String companyId;

    public KeycloakUser(String userId, String name, String email, String role, String company, String companyId) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = Role.valueOf(role);
        this.company = company;
        this.companyId = companyId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }
}
