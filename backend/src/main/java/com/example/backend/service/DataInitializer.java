package com.example.backend.service;

import com.example.backend.model.*;
import com.example.backend.service.CompanyService;
import com.example.backend.repository.*;
import com.example.backend.util.LoggerUtil;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Configuration
public class DataInitializer implements CommandLineRunner {

    @Value("${app.initializeData}")
    private boolean initializeData;

    @Value("${app.keycloak-users-file}")
    private String keycloakUsersFile;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CompanyCourseRepository companyCourseRepository;

    @Autowired
    private UserProgressRepository userProgressRepository;

    @Autowired
    private EnrolledEmployeeRepository enrolledEmployeeRepository;

    @Autowired
    private CompanyService companyService; // Autowire CompanyService

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (initializeData) {
            System.out.println("Starting data initialization...");
            List<Company> companies = initializeCompanies();
            List<Course> courses = initializeCourses();
            assignCoursesToCompanies(companies, courses); // Assign courses before users

            List<KeycloakUser> keycloakUsers = readKeycloakUsersFromCSV(keycloakUsersFile);
            assignKeycloakUsersToCompanies(keycloakUsers);
            assignCoursesAndProgress(keycloakUsers);
            System.out.println("Data initialization completed.");
        }
    }

    private List<Company> initializeCompanies() {
        System.out.println("Initializing companies...");
        List<Company> companies = Arrays.asList(
                createCompany(1L, "Google", "google.com"),
                createCompany(2L, "Microsoft", "microsoft.com"),
                createCompany(3L, "Apple", "apple.com"),
                createCompany(4L, "Amazon", "amazon.com"),
                createCompany(5L, "IBM", "ibm.com"),
                createCompany(6L, "Facebook", "facebook.com"),
                createCompany(7L, "Twitter", "twitter.com"),
                createCompany(8L, "LinkedIn", "linkedin.com"),
                createCompany(9L, "Netflix", "netflix.com"),
                createCompany(10L, "Tesla", "tesla.com"),
                createCompany(11L, "Samsung", "samsung.com"),
                createCompany(12L, "Intel", "intel.com"),
                createCompany(13L, "Oracle", "oracle.com"),
                createCompany(14L, "Adobe", "adobe.com"),
                createCompany(15L, "Salesforce", "salesforce.com"),
                createCompany(16L, "Cisco", "cisco.com"),
                createCompany(17L, "HP", "hp.com"),
                createCompany(18L, "Sony", "sony.com"),
                createCompany(19L, "Dell", "dell.com"),
                createCompany(20L, "Nvidia", "nvidia.com")
        );
        System.out.println("{} companies initialized " + companies.size());
        return companyRepository.saveAll(companies);
    }

    private List<Course> initializeCourses() {
        System.out.println("Initializing courses...");
        List<Course> courses = Arrays.asList(
                createCourse("Introduction to IT", "Basic concepts and principles of Information Technology."),
                createCourse("Network Security", "Fundamentals of securing network infrastructure."),
                createCourse("Cybersecurity Basics", "Introduction to cybersecurity principles and practices."),
                createCourse("Cloud Computing", "Overview of cloud computing technologies and services."),
                createCourse("Data Protection", "Methods and techniques for protecting data integrity and privacy."),
                createCourse("Ethical Hacking", "Basics of ethical hacking and penetration testing."),
                createCourse("IT Project Management", "Managing IT projects and ensuring successful delivery."),
                createCourse("Incident Response", "Handling and responding to security incidents effectively."),
                createCourse("Cryptography", "Principles and practices of data encryption and decryption."),
                createCourse("Risk Management", "Identifying and mitigating risks in IT environments."),
                createCourse("Security Governance", "Frameworks and policies for managing IT security."),
                createCourse("Security Architecture", "Designing secure IT infrastructures."),
                createCourse("Malware Analysis", "Techniques for analyzing and mitigating malware threats."),
                createCourse("Security Auditing", "Conducting security audits and assessments."),
                createCourse("Digital Forensics", "Investigating and analyzing digital evidence."),
                createCourse("Identity and Access Management", "Managing user identities and access controls."),
                createCourse("Mobile Security", "Securing mobile devices and applications."),
                createCourse("Application Security", "Protecting software applications from threats."),
                createCourse("Compliance and Legal Issues", "Understanding legal and regulatory requirements for IT security."),
                createCourse("Vulnerability Management", "Identifying and addressing security vulnerabilities."),
                createCourse("Penetration Testing", "Advanced techniques for penetration testing and vulnerability assessment."),
                createCourse("Security Awareness Training", "Educating users on security best practices."),
                createCourse("Wireless Security", "Securing wireless networks and communications."),
                createCourse("DevSecOps", "Integrating security into DevOps practices."),
                createCourse("IoT Security", "Securing Internet of Things devices and networks.")
        );
        System.out.println("{} courses initialized " + courses.size());
        return courseRepository.saveAll(courses);
    }

    private Company createCompany(Long companyId, String name, String emailEnding) {
        Company company = new Company();
        company.setCompanyId(companyId);
        company.setName(name);
        company.setCompanyEmailEnding(emailEnding);
        company.setRegisterUser(0);
        company.setMaxUser(generateRandomMaxUser());
        return company;
    }

    private int generateRandomMaxUser() {
        Random random = new Random();
        return 100 + random.nextInt(151);
    }

    private Course createCourse(String name, String description) {
        Course course = new Course();
        course.setName(name);
        course.setDescription(description);
        return course;
    }

    private void assignCoursesToCompanies(List<Company> companies, List<Course> courses) {
        System.out.println("Assigning courses to companies...");
        Random random = new Random();
        companies.forEach(company -> {
            for (Course course : courses) {
                if (random.nextBoolean()) {
                    CompanyCourse companyCourse = new CompanyCourse(company, course);
                    companyCourseRepository.save(companyCourse); // Save the relationship
                }
            }
        });
        System.out.println("Courses assigned to companies");
    }

    private List<KeycloakUser> readKeycloakUsersFromCSV(String filename) throws IOException {
        System.out.println("Reading Keycloak users from CSV: " + filename);
        List<KeycloakUser> users = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            int lineNumber = 0;
            boolean skipHeader = true; // Flag to skip the first line (header)
            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (skipHeader) {
                    skipHeader = false; // Skip the first line
                    continue;
                }

                String[] values = line.split(",");
                // Ensure the array has enough elements before creating KeycloakUser
                if (values.length >= 6) {
                    users.add(new KeycloakUser(values[0], values[1], values[2], values[3], values[4], values[5]));
                } else {
                    System.err.println("Skipping invalid line: " + line); // Print error if line is invalid
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            throw e; // Rethrow the IOException to propagate the error
        }
        System.out.println(users.size() + " Keycloak users read from CSV");
        return users;
    }

    private void assignKeycloakUsersToCompanies(List<KeycloakUser> keycloakUsers) {
        System.out.println("Assigning Keycloak users to companies...");
        for (KeycloakUser keycloakUser : keycloakUsers) {
            String companyDomain = keycloakUser.getEmail().split("@")[1];
            Company company = companyRepository.findByCompanyEmailEnding(companyDomain)
                    .orElseThrow(() -> new RuntimeException("Company not found for domain: " + companyDomain));

            EnrolledEmployee enrolledEmployee = new EnrolledEmployee();
            enrolledEmployee.setCompany(company);
            enrolledEmployee.setKeycloakUserId(keycloakUser.getUserId());
            enrolledEmployeeRepository.save(enrolledEmployee);

            company.setRegisterUser(company.getRegisterUser() + 1);
            companyRepository.save(company);
        }
        System.out.println("Keycloak users assigned to companies");
    }

    private void assignCoursesAndProgress(List<KeycloakUser> keycloakUsers) {
        System.out.println("Assigning courses and progress to users...");
        Random random = new Random();

        for (KeycloakUser keycloakUser : keycloakUsers) {
            Company company = companyRepository.findByCompanyEmailEnding(keycloakUser.getEmail().split("@")[1])
                    .orElseThrow(() -> new RuntimeException("Company not found for user: " + keycloakUser.getName()));

            List<Course> bookedCourses = companyService.getBookedCourses(company.getCompanyId()); // Use companyService here

            if (bookedCourses.isEmpty()) {
                continue; // Skip if the company has no courses booked
            }

            int numCoursesToAssign = random.nextInt(bookedCourses.size()) + 1;
            Collections.shuffle(bookedCourses);

            for (int i = 0; i < numCoursesToAssign; i++) {
                Course course = bookedCourses.get(i);
                Optional<UserProgress> existingProgress = userProgressRepository.findByKeycloakUserIdAndCourse(keycloakUser.getUserId(), course);

                if (existingProgress.isEmpty()) {
                    UserProgress userProgress = new UserProgress();
                    userProgress.setKeycloakUserId(keycloakUser.getUserId());
                    userProgress.setCourse(course);
                    userProgress.setCompanyId(company.getCompanyId()); // Set companyId explicitly
                    userProgress.setProgress(random.nextInt(51));
                    userProgressRepository.save(userProgress);
                }
            }
        }
        System.out.println("Courses and progress assigned to users");
    }
}