package com.example.backend.controller;

import com.example.backend.model.Course;
import com.example.backend.model.UserProgress;
import com.example.backend.model.Users;
import com.example.backend.service.UserService;
import com.example.backend.service.UserProgressService;
import com.example.backend.repository.CourseRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;



@RestController
@RequestMapping("/api/users")
public class UsersController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserProgressService userProgressService;

    @Autowired
    private CourseRepository courseRepository;

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id, HttpSession session) {
        Long sessionUserId = (Long) session.getAttribute("userId");
        if (sessionUserId == null || !id.equals(sessionUserId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Optional<Users> user = userService.findUserById(id);

        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return new ResponseEntity<>("User not found", HttpStatus.NO_CONTENT);
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Users> updateUser(@PathVariable Long id, @RequestBody Map<String, String> updates, HttpSession session) {
        Long sessionUserId = (Long) session.getAttribute("userId");
        if (!id.equals(sessionUserId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Optional<Users> userOptional = userService.findUserById(id);
        if (userOptional.isPresent()) {
            Users user = userOptional.get();
            updates.forEach((key, value) -> {
                switch (key) {
                    case "name":
                        user.setName(value);
                        break;
                    case "password":
                        user.setPassword(value);
                        break;
                    default:
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid field: " + key);
                }
            });
            userService.saveUser(user);
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{userId}/book-course")
    public ResponseEntity<UserProgress> bookCourse(@PathVariable Long userId, @RequestBody Map<String, Long> request, HttpSession session) {
        Long sessionUserId = (Long) session.getAttribute("userId");
        if (!userId.equals(sessionUserId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Long courseId = request.get("courseId");
        Optional<Users> userOptional = userService.findUserById(userId);
        Optional<Course> courseOptional = courseRepository.findById(courseId);
        if (userOptional.isPresent() && courseOptional.isPresent()) {
            Users user = userOptional.get();
            Course course = courseOptional.get();
            UserProgress userProgress = new UserProgress();
            userProgress.setUser(user);
            userProgress.setCourse(course);
            userProgress.setProgress(0); // Initial progress is 0
            userProgressService.saveProgress(userProgress);
            return ResponseEntity.ok(userProgress);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

