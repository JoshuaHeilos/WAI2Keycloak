package com.example.backend.controller;

import com.example.backend.model.Course;
import com.example.backend.service.CourseService;
import com.example.backend.util.JwtUtils;
import com.example.backend.util.LoggerUtil;
import com.example.backend.util.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        List<Course> courses = courseService.getAllCourses();
        return courses.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(courses);
    }

    @PostMapping
    public ResponseEntity<?> addCourse(@RequestBody Course course, Authentication authentication) {
        try {
            UserInfo userInfo = JwtUtils.extractUserInfo(authentication);
            LoggerUtil.log("User Info for adding course: " + userInfo);
            if (!courseService.isUserAdmin(userInfo)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not authorized to add courses");
            }
            return ResponseEntity.ok(courseService.addCourse(course));
        } catch (Exception e) {
            LoggerUtil.log("Error adding course: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while adding the course");
        }
    }

    @PutMapping("/{courseId}")
    public ResponseEntity<?> updateCourse(@PathVariable Long courseId, @RequestBody Course course, Authentication authentication) {
        try {
            UserInfo userInfo = JwtUtils.extractUserInfo(authentication);
            LoggerUtil.log("User Info for updating course: " + userInfo);
            if (!courseService.isUserAdmin(userInfo)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not authorized to update courses");
            }
            return ResponseEntity.ok(courseService.updateCourse(courseId, course));
        } catch (Exception e) {
            LoggerUtil.log("Error updating course: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating the course");
        }
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long courseId, Authentication authentication) {
        try {
            UserInfo userInfo = JwtUtils.extractUserInfo(authentication);
            LoggerUtil.log("User Info for deleting course: " + userInfo);
            if (!courseService.isUserAdmin(userInfo)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not authorized to delete courses");
            }
            courseService.deleteCourse(courseId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            LoggerUtil.log("Error deleting course: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while deleting the course");
        }
    }
}