package com.example.backend.controller;

import com.example.backend.model.SessionInfo;
import com.example.backend.model.Users;
import com.example.backend.service.UserService;
import com.example.backend.util.LoggerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Users loginRequest, HttpSession session) {
        System.out.println("Login attempt for email: " + loginRequest.getCompanyEmail());
        Optional<Users> user = userService.validateUser(loginRequest.getCompanyEmail(), loginRequest.getPassword());
        if (user.isPresent()) {
            Users loggedInUser = user.get();
            session.setAttribute("userId", loggedInUser.getUserId());
            session.setAttribute("role", loggedInUser.getRole().name());
            session.setAttribute("companyId", loggedInUser.getCompany().getCompanyId());
            LoggerUtil.log("Session attributes set: userId=" + loggedInUser.getUserId() + ", role=" + loggedInUser.getRole() + ", companyId=" + loggedInUser.getCompany().getCompanyId());
            return ResponseEntity.ok(loggedInUser);
        } else {
            LoggerUtil.log("Invalid login attempt for email: " + loginRequest.getCompanyEmail());
            return ResponseEntity.status(401).body("Invalid email or password");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Users user) {
        try {
            userService.registerUser(user);
            return ResponseEntity.ok("User registered successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @GetMapping("/session-info")
    public ResponseEntity<?> getSessionInfo(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        String roleString = (String) session.getAttribute("role");
        Long companyId = (Long) session.getAttribute("companyId");
        String sessionId = session.getId();

        LoggerUtil.log("Fetching session info: sessionId=" + sessionId + ", userId=" + userId + ", role=" + roleString + ", companyId=" + companyId);

        if (userId == null || roleString == null || companyId == null) {
            LoggerUtil.log("No active session");
            return ResponseEntity.status(401).body("No active session");
        }

        return ResponseEntity.ok(new SessionInfo(sessionId, userId, roleString, companyId));
    }
}
