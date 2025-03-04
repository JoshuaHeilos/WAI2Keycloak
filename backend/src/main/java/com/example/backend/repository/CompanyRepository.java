package com.example.backend.repository;

import com.example.backend.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByCompanyEmailEnding(String companyEmailEnding);
}
