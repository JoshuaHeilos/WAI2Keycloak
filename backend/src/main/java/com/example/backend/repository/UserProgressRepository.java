package com.example.backend.repository;

import com.example.backend.model.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    List<UserProgress> findByUsersUserId(Long userId);
}

