package com.example.backend.service;

import com.example.backend.model.Course;
import com.example.backend.model.UserProgress;
import com.example.backend.repository.CourseRepository;
import com.example.backend.repository.UserProgressRepository;
import com.example.backend.util.UserInfo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserProgressService {

    @Autowired
    private UserProgressRepository userProgressRepository;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private CourseRepository courseRepository;

    public List<UserProgress> findProgressByKeycloakUserId(String keycloakUserId) {
        return userProgressRepository.findByKeycloakUserId(keycloakUserId);
    }

    public Optional<UserProgress> findProgressById(Long progressId) {
        return userProgressRepository.findById(progressId);
    }

    public UserProgress saveProgress(UserProgress progress) {
        return userProgressRepository.save(progress);
    }

    @Transactional
    public UserProgress bookCourse(UserInfo userInfo, Course course) {
        // Check if the user has already booked this course
        Optional<UserProgress> existingProgress = userProgressRepository.findByKeycloakUserIdAndCourse(userInfo.getUserId(), course);
        if (existingProgress.isPresent()) {
            throw new IllegalStateException("User has already booked this course");
        }

        // Check if the user is authorized for this company
        if (!authorizationService.isUserAuthorizedForCompany(userInfo, Long.parseLong(userInfo.getCompanyId()))) {
            throw new IllegalStateException("User is not authorized for this company");
        }

        UserProgress userProgress = new UserProgress();
        userProgress.setKeycloakUserId(userInfo.getUserId());
        userProgress.setCourse(course);
        userProgress.setCompanyId(Long.parseLong(userInfo.getCompanyId()));
        userProgress.setProgress(0);

        return userProgressRepository.save(userProgress);
    }

    @Transactional
    public UserProgress updateProgress(UserInfo userInfo, Long courseId, int newProgress) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalStateException("Course not found"));

        Optional<UserProgress> existingProgress = userProgressRepository.findByKeycloakUserIdAndCourse(userInfo.getUserId(), course);
        if (existingProgress.isEmpty()) {
            throw new IllegalStateException("User has not booked this course yet");
        }

        // Check if the user is authorized for this company
        if (!authorizationService.isUserAuthorizedForCompany(userInfo, Long.parseLong(userInfo.getCompanyId()))) {
            throw new IllegalStateException("User is not authorized for this company");
        }

        UserProgress userProgress = existingProgress.get();
        userProgress.setProgress(newProgress);

        return userProgressRepository.save(userProgress);
    }
}

