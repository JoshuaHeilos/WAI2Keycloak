package com.example.backend.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.Set;

@Entity
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long companyId;
    private String name;
    @Column(unique = true)
    private String companyEmailEnding;
    private Integer registerUser;
    private Integer maxUser;

    @OneToMany(mappedBy = "company")
    @JsonIgnore
    private Set<Users> users;

    @OneToMany(mappedBy = "company")
    @JsonManagedReference
    private Set<CompanyCourse> companyCourses;

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

    public Set<Users> getUsers() {
        return users;
    }

    public void setUsers(Set<Users> users) {
        this.users = users;
    }

    public Set<CompanyCourse> getCompanyCourses() {
        return companyCourses;
    }

    public void setCompanyCourses(Set<CompanyCourse> companyCourses) {
        this.companyCourses = companyCourses;
    }
}
