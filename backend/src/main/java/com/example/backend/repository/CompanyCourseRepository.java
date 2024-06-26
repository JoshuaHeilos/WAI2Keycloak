package com.example.backend.repository;

import com.example.backend.model.Company;
import com.example.backend.model.CompanyCourse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyCourseRepository extends JpaRepository<CompanyCourse, Long> {
    List<CompanyCourse> findByCompany(Company company);
}
