package com.example.backend.controller;

import com.example.backend.model.UserProgress;
import com.example.backend.service.UserProgressService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user-progress")
public class UserProgressController {

    @Autowired
    private UserProgressService userProgressService;

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserProgress(@PathVariable Long userId, HttpSession session) {
        Long sessionUserId = (Long) session.getAttribute("userId");
        if (sessionUserId == null || !userId.equals(sessionUserId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        List<UserProgress> progressList = userProgressService.findProgressByUserId(userId);

        if (progressList == null || progressList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        // Transform the response to only include course data, progress, and id
        List<Map<String, Object>> simplifiedProgressList = progressList.stream().map(progress -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", progress.getId()); // Include id
            map.put("courseId", progress.getCourse().getCourseId());
            map.put("courseName", progress.getCourse().getName());
            map.put("courseDescription", progress.getCourse().getDescription());
            map.put("progress", progress.getProgress());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(simplifiedProgressList);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserProgress> updateProgress(@PathVariable Long id, @RequestBody Map<String, Integer> updates, HttpSession session) {
        Long sessionUserId = (Long) session.getAttribute("userId");
        Optional<UserProgress> progressOptional = userProgressService.findProgressById(id);

        if (progressOptional.isPresent() && progressOptional.get().getUser().getUserId().equals(sessionUserId)) {
            UserProgress progress = progressOptional.get();

            if (updates.containsKey("progress")) {
                int newProgress = updates.get("progress");
                progress.setProgress(newProgress);

                // Log user ID and the progress update
                System.out.println("User ID: " + sessionUserId + " updated progress to: " + newProgress);
            }

            userProgressService.saveProgress(progress);
            return ResponseEntity.ok(progress);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }
}
