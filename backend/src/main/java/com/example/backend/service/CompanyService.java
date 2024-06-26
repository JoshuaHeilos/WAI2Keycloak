package com.example.backend.service;

import com.example.backend.model.*;
import com.example.backend.repository.CompanyRepository;
import com.example.backend.repository.CompanyCourseRepository;
import com.example.backend.repository.CourseRepository;
import com.example.backend.repository.UserProgressRepository;
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

    public Optional<Company> findCompanyById(Long id) {
        return companyRepository.findById(id);
    }

    public List<Company> findAllCompanies() {
        return companyRepository.findAll();
    }

    public void saveCompany(Company company) {
        companyRepository.save(company);
    }

    public List<CompanyDTO> findAllCompanyDTOs() {
        return companyRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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

    public List<Course> getAvailableCoursesForUser(Long companyId, Long userId) {
        Company company = companyRepository.findById(companyId).orElseThrow(() -> new RuntimeException("Company not found"));
        List<UserProgress> userProgressList = userProgressRepository.findByUsersUserId(userId);
        Set<Long> userCourseIds = userProgressList.stream().map(up -> up.getCourse().getCourseId()).collect(Collectors.toSet());

        List<CompanyCourse> companyCourses = companyCourseRepository.findByCompany(company);
        Set<Long> companyCourseIds = companyCourses.stream().map(cc -> cc.getCourse().getCourseId()).collect(Collectors.toSet());

        return courseRepository.findAll().stream()
                .filter(course -> !userCourseIds.contains(course.getCourseId()) && companyCourseIds.contains(course.getCourseId()))
                .collect(Collectors.toList());
    }

    public List<Course> getAvailableCourses(Long companyId) {
        Company company = companyRepository.findById(companyId).orElseThrow(() -> new RuntimeException("Company not found"));
        List<CompanyCourse> companyCourses = companyCourseRepository.findByCompany(company);
        Set<Long> companyCourseIds = companyCourses.stream().map(cc -> cc.getCourse().getCourseId()).collect(Collectors.toSet());

        return courseRepository.findAll().stream()
                .filter(course -> !companyCourseIds.contains(course.getCourseId()))
                .collect(Collectors.toList());
    }

    public List<Course> getBookedCourses(Long companyId) {
        Company company = companyRepository.findById(companyId).orElseThrow(() -> new RuntimeException("Company not found"));
        List<CompanyCourse> companyCourses = companyCourseRepository.findByCompany(company);
        return companyCourses.stream()
                .map(CompanyCourse::getCourse)
                .collect(Collectors.toList());
    }

    public CompanyCourse bookCourseForCompany(Long companyId, Long courseId) {
        Company company = companyRepository.findById(companyId).orElseThrow(() -> new RuntimeException("Company not found"));
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found"));

        // Check if the course is already booked by the company
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

    public void deleteCourseForCompany(Long companyId, Long courseId) {
        Company company = companyRepository.findById(companyId).orElseThrow(() -> new RuntimeException("Company not found"));
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found"));

        CompanyCourse companyCourse = companyCourseRepository.findByCompany(company).stream()
                .filter(cc -> cc.getCourse().getCourseId().equals(courseId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Course not booked by the company"));

        // Remove the course from booked courses
        companyCourseRepository.delete(companyCourse);

        // Remove progress records for this course for all employees of the company
        List<UserProgress> userProgressList = userProgressRepository.findAll().stream()
                .filter(up -> up.getCourse().getCourseId().equals(courseId) && up.getUser().getCompany().getCompanyId().equals(companyId))
                .collect(Collectors.toList());
        userProgressRepository.deleteAll(userProgressList);
    }
}
