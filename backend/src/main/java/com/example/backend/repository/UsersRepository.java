package com.example.backend.repository;

import com.example.backend.model.Company;
import com.example.backend.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByCompanyEmailAndPassword(String companyEmail, String password);
    Optional<Users> findByCompanyEmailAndCompany(String companyEmail, Company company);
}
