package com.example.backend.controller;

import com.example.backend.model.Course;
import com.example.backend.model.UserProgress;
import com.example.backend.repository.CourseRepository;
import com.example.backend.service.AuthorizationService;
import com.example.backend.service.UserProgressService;
import com.example.backend.util.JwtUtils;
import com.example.backend.util.LoggerUtil;
import com.example.backend.util.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user-progress")
public class UserProgressController {

    @Autowired
    private UserProgressService userProgressService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private AuthorizationService authorizationService;

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserProgress(Authentication authentication) {
        try {
            UserInfo userInfo = JwtUtils.extractUserInfo(authentication);
            LoggerUtil.log("User Info for getting progress: " + userInfo);

            if (!authorizationService.isUserAuthorizedForUser(userInfo, userInfo.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not authorized to access this progress");
            }

            List<UserProgress> progressList = userProgressService.findProgressByKeycloakUserId(userInfo.getUserId());

            if (progressList.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            List<Map<String, Object>> simplifiedProgressList = progressList.stream().map(progress -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", progress.getProgressId());
                map.put("courseId", progress.getCourse().getCourseId());
                map.put("courseName", progress.getCourse().getName());
                map.put("courseDescription", progress.getCourse().getDescription());
                map.put("progress", progress.getProgress());
                return map;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(simplifiedProgressList);
        } catch (Exception e) {
            LoggerUtil.log("Error getting user progress: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while fetching user progress");
        }
    }

    @PostMapping("/{userId}/book-course/{courseId}")
    public ResponseEntity<?> bookCourse(@PathVariable String userId, @PathVariable Long courseId,
                                        Authentication authentication) {
        try {
            UserInfo userInfo = JwtUtils.extractUserInfo(authentication);
            LoggerUtil.log("User Info for booking course: " + userInfo);

            if (!authorizationService.isUserAuthorizedForUser(userInfo, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not authorized to book course for this user ID");
            }

            Optional<Course> courseOptional = courseRepository.findById(courseId);
            if (courseOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Course course = courseOptional.get();
            UserProgress userProgress = userProgressService.bookCourse(userInfo, course);
            System.out.println(userInfo.getUserId() + " booked course ID: " + courseId + " for user ID: " + userId);
            return ResponseEntity.ok(userProgress);
        } catch (IllegalStateException e) {
            LoggerUtil.log("Error booking course: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            LoggerUtil.log("Error booking course: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while booking the course");
        }
    }

    @PatchMapping("/{userId}/progress/{courseId}")
    public ResponseEntity<?> updateProgress(@PathVariable String userId,
                                            @PathVariable Long courseId,
                                            @RequestBody Map<String, Integer> updates,
                                            Authentication authentication) {
        try {
            UserInfo userInfo = JwtUtils.extractUserInfo(authentication);
            System.out.println("User Info for updating progress: " + userInfo + ", on course ID: " + courseId);

            if (!authorizationService.isUserAuthorizedForUser(userInfo, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not authorized to update progress for this user ID");
            }

            if (!updates.containsKey("progress")) {
                return ResponseEntity.badRequest().body("Progress update must be provided in the request body");
            }

            int newProgress = updates.get("progress");
            UserProgress updatedProgress = userProgressService.updateProgress(userInfo, courseId, newProgress);
            System.out.println(userInfo.getUserId() + " updated progress on course ID: " + courseId + " to " + newProgress + "%");
            return ResponseEntity.ok(updatedProgress);
        } catch (IllegalStateException e) {
            LoggerUtil.log("Error updating progress: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            LoggerUtil.log("Error updating progress: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating the progress");
        }
    }
}
