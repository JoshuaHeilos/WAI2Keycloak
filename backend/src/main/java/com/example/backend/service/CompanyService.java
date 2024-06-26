package com.example.backend.service;

import com.example.backend.model.*;
import com.example.backend.repository.CompanyCourseRepository;
import com.example.backend.repository.CompanyRepository;
import com.example.backend.repository.CourseRepository;
import com.example.backend.repository.UserProgressRepository;
import com.example.backend.util.LoggerUtil;
import com.example.backend.util.UserInfo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserProgressRepository userProgressRepository;

    @Autowired
    private CompanyCourseRepository companyCourseRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private AuthorizationService authorizationService;

    public Optional<Company> findCompanyById(Long id) {
        return companyRepository.findById(id);
    }

    public List<CompanyDTO> findAllCompanyDTOs() {
        return companyRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public void saveCompany(Company company) {
        companyRepository.save(company);
    }

    public List<Course> getAvailableCoursesForUser(Long companyId, String keycloakUserId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        List<UserProgress> userProgressList = userProgressRepository.findByKeycloakUserId(keycloakUserId);
        Set<Long> userCourseIds = userProgressList.stream()
                .map(up -> up.getCourse().getCourseId())
                .collect(Collectors.toSet());

        List<CompanyCourse> companyCourses = companyCourseRepository.findByCompany(company);
        Set<Long> companyCourseIds = companyCourses.stream()
                .map(cc -> cc.getCourse().getCourseId())
                .collect(Collectors.toSet());

        return courseRepository.findAll().stream()
                .filter(course -> !userCourseIds.contains(course.getCourseId()) && companyCourseIds.contains(course.getCourseId()))
                .collect(Collectors.toList());
    }

    public List<Course> getAvailableCourses(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        List<CompanyCourse> companyCourses = companyCourseRepository.findByCompany(company);
        Set<Long> companyCourseIds = companyCourses.stream()
                .map(cc -> cc.getCourse().getCourseId())
                .collect(Collectors.toSet());

        return courseRepository.findAll().stream()
                .filter(course -> !companyCourseIds.contains(course.getCourseId()))
                .collect(Collectors.toList());
    }

    public List<Course> getBookedCourses(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        List<CompanyCourse> companyCourses = companyCourseRepository.findByCompany(company);
        return companyCourses.stream()
                .map(CompanyCourse::getCourse)
                .collect(Collectors.toList());
    }

    public CompanyCourse bookCourseForCompany(Long companyId, Long courseId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        boolean alreadyBooked = companyCourseRepository.findByCompany(company).stream()
                .anyMatch(cc -> cc.getCourse().getCourseId().equals(courseId));

        if (alreadyBooked) {
            throw new RuntimeException("Course is already booked by the company");
        }

        CompanyCourse companyCourse = new CompanyCourse();
        companyCourse.setCompany(company);
        companyCourse.setCourse(course);
        return companyCourseRepository.save(companyCourse);
    }

    @Transactional
    public void deleteCourseForCompany(Long companyId, Long courseId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        CompanyCourse companyCourse = companyCourseRepository.findByCompanyCompanyIdAndCourseCourseId(companyId, courseId)
                .orElseThrow(() -> new RuntimeException("Course not booked by the company"));

        companyCourseRepository.delete(companyCourse);

        // Delete all UserProgress entries for this course and company
        List<UserProgress> userProgressList = userProgressRepository.findByCourse_CourseIdAndCompanyId(courseId, companyId);
        userProgressRepository.deleteAll(userProgressList);

        // Remove the course from the company's set of courses
        company.getCompanyCourses().removeIf(cc -> cc.getCourse().getCourseId().equals(courseId));
        companyRepository.save(company);
    }



    public boolean isUserAuthorizedForCompany(UserInfo userInfo, Long companyId) {
        return authorizationService.isUserAuthorizedForCompany(userInfo, companyId);
    }

    public boolean isUserTeamLeader(UserInfo userInfo) {
        return authorizationService.isUserTeamLeader(userInfo);
    }

    public boolean isUserAdmin(UserInfo userInfo) {
        return authorizationService.isUserAdmin(userInfo);
    }


    private CompanyDTO convertToDTO(Company company) {
        CompanyDTO dto = new CompanyDTO();
        dto.setCompanyId(company.getCompanyId());
        dto.setName(company.getName());
        dto.setCompanyEmailEnding(company.getCompanyEmailEnding());
        dto.setRegisterUser(company.getRegisterUser());
        dto.setMaxUser(company.getMaxUser());
        return dto;
    }



}