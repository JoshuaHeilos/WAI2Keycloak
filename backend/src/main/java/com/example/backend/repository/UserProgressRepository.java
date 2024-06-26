package com.example.backend.repository;

import com.example.backend.model.Course;
import com.example.backend.model.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    List<UserProgress> findByKeycloakUserId(String keycloakUserId);
    List<UserProgress> findByCourse_CourseIdAndCompanyId(Long courseId, Long companyId);
    Optional<UserProgress> findByKeycloakUserIdAndCourse(String keycloakUserId, Course course);
}


