package com.example.backend.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.Set;

@Entity
public class Company {

    @Id
    private Long companyId;
    private String name;
    @Column(unique = true)
    private String companyEmailEnding;
    private Integer registerUser;
    private Integer maxUser;

    @OneToMany(mappedBy = "company")
    @JsonIgnore // You no longer need this relationship for users
    private Set<EnrolledEmployee> enrolledEmployees; // Relationship with EnrolledEmployee

    @OneToMany(mappedBy = "company")
    @JsonManagedReference
    private Set<CompanyCourse> companyCourses;

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

    public Set<EnrolledEmployee> getEnrolledEmployees() {
        return enrolledEmployees;
    }

    public void setEnrolledEmployees(Set<EnrolledEmployee> enrolledEmployees) {
        this.enrolledEmployees = enrolledEmployees;
    }

    public Set<CompanyCourse> getCompanyCourses() {
        return companyCourses;
    }


    public void setCompanyCourses(Set<CompanyCourse> companyCourses) {
        this.companyCourses = companyCourses;
    }
}
