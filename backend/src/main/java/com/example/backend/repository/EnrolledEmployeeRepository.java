package com.example.backend.repository;

import com.example.backend.model.EnrolledEmployee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrolledEmployeeRepository extends JpaRepository<EnrolledEmployee, Long> {
}

