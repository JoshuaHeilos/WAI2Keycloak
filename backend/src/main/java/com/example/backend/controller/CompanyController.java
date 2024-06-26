package com.example.backend.controller;

import com.example.backend.model.Company;
import com.example.backend.model.CompanyCourse;
import com.example.backend.model.CompanyDTO;
import com.example.backend.model.Course;
import com.example.backend.service.CompanyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @GetMapping("/{id}")
    public ResponseEntity<Company> getCompanyById(@PathVariable Long id, HttpSession session) {
        Optional<Company> company = companyService.findCompanyById(id);
        return company.map(ResponseEntity::ok).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/{companyId}/booked-courses")
    public ResponseEntity<List<Course>> getBookedCourses(@PathVariable Long companyId, HttpSession session) {
        if (!isAuthorizedTeamLeader(session, companyId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<Course> bookedCourses = companyService.getBookedCourses(companyId);
        if (bookedCourses.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(bookedCourses);
    }

    @PostMapping("/{companyId}/book-course")
    public ResponseEntity<?> bookCourseForCompany(@PathVariable Long companyId, @RequestBody Map<String, Long> request, HttpSession session) {
        if (!isAuthorizedTeamLeader(session, companyId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Long courseId = request.get("courseId");
        try {
            CompanyCourse companyCourse = companyService.bookCourseForCompany(companyId, courseId);
            return ResponseEntity.ok(companyCourse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/{companyId}/available-courses")
    public ResponseEntity<List<Course>> getAvailableCourses(@PathVariable Long companyId, HttpSession session) {
        if (!isAuthorizedTeamLeader(session, companyId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<Course> availableCourses = companyService.getAvailableCourses(companyId);
        if (availableCourses.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(availableCourses);
        }
        return ResponseEntity.ok(availableCourses);
    }

    @DeleteMapping("/{companyId}/delete-course")
    public ResponseEntity<?> deleteCourseForCompany(@PathVariable Long companyId, @RequestBody Map<String, Long> request, HttpSession session) {
        if (!isAuthorizedTeamLeader(session, companyId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Long courseId = request.get("courseId");
        try {
            companyService.deleteCourseForCompany(companyId, courseId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/{companyId}/available-courses/{userId}")
    public ResponseEntity<List<Course>> getAvailableCoursesForUser(@PathVariable Long companyId, @PathVariable Long userId, HttpSession session) {
        Long sessionCompanyId = (Long) session.getAttribute("companyId");
        if (!sessionCompanyId.equals(companyId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<Course> availableCourses = companyService.getAvailableCoursesForUser(companyId, userId);
        if (availableCourses.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(availableCourses);
    }

    @GetMapping
    public ResponseEntity<List<CompanyDTO>> getAllCompanies() {
        List<CompanyDTO> companies = companyService.findAllCompanyDTOs();
        if (companies.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(companies);
    }

    @PatchMapping("/{companyId}/update-max-users")
    public ResponseEntity<Company> updateMaxUsers(@PathVariable Long companyId, @RequestBody Map<String, Integer> updates, HttpSession session) {
        if (!isAdmin(session)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
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

    private boolean isAuthorizedTeamLeader(HttpSession session, Long companyId) {
        Long sessionCompanyId = (Long) session.getAttribute("companyId");
        String role = (String) session.getAttribute("role");
        return companyId.equals(sessionCompanyId) && "TeamLeader".equals(role);
    }

    private boolean isAdmin(HttpSession session) {
        String role = (String) session.getAttribute("role");
        return "Admin".equals(role);
    }
}

