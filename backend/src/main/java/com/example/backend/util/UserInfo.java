package com.example.backend.util;

public class UserInfo {
    private final String userId;
    private final String companyId;
    private final String role;

    public UserInfo(String userId, String companyId, String role) {
        this.userId = userId;
        this.companyId = companyId;
        this.role = role;
    }

    // Getters
    public String getUserId() { return userId; }
    public String getCompanyId() { return companyId; }
    public String getRole() { return role; }
}