package com.example.backend.model;

public class CompanyDTO {
    private Long companyId;
    private String name;
    private String companyEmailEnding;
    private Integer registerUser;
    private Integer maxUser;

    // Getters and setters
    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompanyEmailEnding() {
        return companyEmailEnding;
    }

    public void setCompanyEmailEnding(String companyEmailEnding) {
        this.companyEmailEnding = companyEmailEnding;
    }

    public Integer getRegisterUser() {
        return registerUser;
    }

    public void setRegisterUser(Integer registerUser) {
        this.registerUser = registerUser;
    }

    public Integer getMaxUser() {
        return maxUser;
    }

    public void setMaxUser(Integer maxUser) {
        this.maxUser = maxUser;
    }
}
