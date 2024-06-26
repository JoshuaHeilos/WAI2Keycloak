package com.example.backend.repository;

import com.example.backend.model.Company;
import com.example.backend.model.CompanyCourse;
import com.example.backend.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyCourseRepository extends JpaRepository<CompanyCourse, Long> {
    List<CompanyCourse> findByCompany(Company company);
    Optional<CompanyCourse> findByCompanyCompanyIdAndCourseCourseId(Long companyId, Long courseId);
}
