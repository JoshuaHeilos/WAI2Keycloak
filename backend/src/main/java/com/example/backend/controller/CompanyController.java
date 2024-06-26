package com.example.backend.controller;

import com.example.backend.model.*;
import com.example.backend.service.CompanyService;
import com.example.backend.util.JwtUtils;
import com.example.backend.util.LoggerUtil;
import com.example.backend.util.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @GetMapping("/{companyId}")
    public ResponseEntity<?> getCompanyById(@PathVariable Long companyId, Authentication authentication) {
        try {
            UserInfo userInfo = JwtUtils.extractUserInfo(authentication);
            LoggerUtil.log("User Info: " + userInfo);
            LoggerUtil.log("Requested Company ID: " + companyId);

            if (!companyService.isUserAuthorizedForCompany(userInfo, companyId)) {
                LoggerUtil.log("User not authorized. User Company ID: " + userInfo.getCompanyId() + ", Requested Company ID: " + companyId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not authorized for this company");
            }

            Optional<Company> company = companyService.findCompanyById(companyId);
            return company.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            LoggerUtil.log("Error processing request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred processing your request");
        }
    }

    @GetMapping("/{companyId}/booked-courses")
    public ResponseEntity<List<Course>> getBookedCourses(@PathVariable Long companyId, Authentication authentication) {
        UserInfo userInfo = JwtUtils.extractUserInfo(authentication);
        if (!companyService.isUserAuthorizedForCompany(userInfo, companyId) ||
                !companyService.isUserTeamLeader(userInfo)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Course> bookedCourses = companyService.getBookedCourses(companyId);
        return bookedCourses.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(bookedCourses);
    }

    @PostMapping("/{companyId}/book-course")
    public ResponseEntity<?> bookCourseForCompany(@PathVariable Long companyId, @RequestBody Map<String, Long> request, Authentication authentication) {
        UserInfo userInfo = JwtUtils.extractUserInfo(authentication);
        if (!companyService.isUserAuthorizedForCompany(userInfo, companyId) ||
                !companyService.isUserTeamLeader(userInfo)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Long courseId = request.get("courseId");
        try {
            CompanyCourse companyCourse = companyService.bookCourseForCompany(companyId, courseId);
            System.out.println(userInfo.getUserId() + " booked course ID: " + courseId + " for company ID: " + companyId);
            return ResponseEntity.ok(companyCourse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/{companyId}/available-courses")
    public ResponseEntity<List<Course>> getAvailableCourses(@PathVariable Long companyId, Authentication authentication) {
        UserInfo userInfo = JwtUtils.extractUserInfo(authentication);
        if (!companyService.isUserAuthorizedForCompany(userInfo, companyId) ||
                !companyService.isUserTeamLeader(userInfo)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Course> availableCourses = companyService.getAvailableCourses(companyId);
        return availableCourses.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(availableCourses);
    }

    @DeleteMapping("/{companyId}/delete-course")
    public ResponseEntity<?> deleteCourseForCompany(@PathVariable Long companyId, @RequestBody Map<String, Long> request, Authentication authentication) {
        LoggerUtil.log("Delete Course Request: " + request);  // Add logging to check the request body
        UserInfo userInfo = JwtUtils.extractUserInfo(authentication);

        if (!request.containsKey("courseId")) {
            return ResponseEntity.badRequest().body("Missing courseId in request");
        }

        Long courseId = request.get("courseId");
        if (!companyService.isUserAuthorizedForCompany(userInfo, companyId) || !companyService.isUserTeamLeader(userInfo)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            companyService.deleteCourseForCompany(companyId, courseId);
            System.out.println(userInfo.getUserId() + " deleted course ID: " + courseId + " for company ID: " + companyId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }


    @GetMapping("/{companyId}/available-courses/{userId}")
    public ResponseEntity<List<Course>> getAvailableCoursesForUser(@PathVariable Long companyId, @PathVariable String userId, Authentication authentication) {
        UserInfo userInfo = JwtUtils.extractUserInfo(authentication);
        if (!companyService.isUserAuthorizedForCompany(userInfo, companyId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Course> availableCourses = companyService.getAvailableCoursesForUser(companyId, userId);
        return availableCourses.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(availableCourses);
    }

    @GetMapping
    public ResponseEntity<List<CompanyDTO>> getAllCompanies(Authentication authentication) {
        UserInfo userInfo = JwtUtils.extractUserInfo(authentication);
        if (!companyService.isUserAdmin(userInfo)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<CompanyDTO> companies = companyService.findAllCompanyDTOs();
        return companies.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(companies);
    }

    @PatchMapping("/{companyId}/update-max-users")
    public ResponseEntity<Company> updateMaxUsers(@PathVariable Long companyId, @RequestBody Map<String, Integer> updates, Authentication authentication) {
        UserInfo userInfo = JwtUtils.extractUserInfo(authentication);
        if (!companyService.isUserAdmin(userInfo)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Company> companyOptional = companyService.findCompanyById(companyId);
        if (companyOptional.isPresent()) {
            Company company = companyOptional.get();
            if (updates.containsKey("maxUser")) {
                company.setMaxUser(updates.get("maxUser"));
            }
            companyService.saveCompany(company);
            return ResponseEntity.ok(company);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}