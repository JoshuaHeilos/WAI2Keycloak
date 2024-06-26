package com.example.backend.model;

public class SessionInfo {
    private String sessionId;
    private Long userId;
    private String role;
    private Long companyId;

    public SessionInfo(String sessionId, Long userId, String role, Long companyId) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.role = role;
        this.companyId = companyId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }
}
