package com.example.backend.service;

import com.example.backend.model.*;
import com.example.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.*;

@Configuration
public class DataInitializer implements CommandLineRunner {

    @Value("${app.initializeData}")
    private boolean initializeData;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CompanyCourseRepository companyCourseRepository;

    @Autowired
    private UserProgressRepository userProgressRepository;

    @Autowired
    private EnrolledEmployeeRepository enrolledEmployeeRepository;

    private static final List<String> NAMES = Arrays.asList(
            "Emma", "Liam", "Olivia", "Noah", "Ava", "William", "Sophia", "James",
            "Isabella", "Benjamin", "Mia", "Lucas", "Charlotte", "Henry", "Amelia",
            "Alexander", "Evelyn", "Mason", "Abigail", "Michael", "Harper", "Ethan",
            "Ella", "Daniel", "Grace", "Matthew", "Scarlett", "Aiden", "Victoria",
            "Joseph", "Aria", "Samuel", "Chloe", "David", "Lily", "John", "Zoe",
            "Wyatt", "Nora", "Carter", "Mila", "Jack", "Layla", "Luke", "Aubrey",
            "Owen", "Hannah", "Gabriel", "Ellie", "Caleb"
    );

    private static final List<Role> NON_ADMIN_ROLES = Arrays.asList(
            Role.Employee, Role.Employee, Role.Employee, Role.TeamLeader
    );

    @Override
    public void run(String... args) {
        if (initializeData) {
            System.out.println("Starting data initialization...");
            initializeDatabase();
            System.out.println("Data initialization completed.");

        }
    }

    private void initializeDatabase() {
        List<Company> companies = Arrays.asList(
                createCompany("Google", "google.com"),
                createCompany("Microsoft", "microsoft.com"),
                createCompany("Apple", "apple.com"),
                createCompany("Amazon", "amazon.com"),
                createCompany("IBM", "ibm.com"),
                createCompany("Facebook", "facebook.com"),
                createCompany("Twitter", "twitter.com"),
                createCompany("LinkedIn", "linkedin.com"),
                createCompany("Netflix", "netflix.com"),
                createCompany("Tesla", "tesla.com"),
                createCompany("Adobe", "adobe.com"),
                createCompany("Oracle", "oracle.com"),
                createCompany("Salesforce", "salesforce.com"),
                createCompany("Zoom", "zoom.us"),
                createCompany("Slack", "slack.com"),
                createCompany("Spotify", "spotify.com"),
                createCompany("Dropbox", "dropbox.com"),
                createCompany("Airbnb", "airbnb.com"),
                createCompany("Uber", "uber.com"),
                createCompany("HDA", "HDA.com")
        );

        companyRepository.saveAll(companies);

        // Initialize courses
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

        courseRepository.saveAll(courses);

        Random random = new Random();

        // Assign random courses to companies
        companies.forEach(company -> {
            for (Course course : courses) {
                if (random.nextBoolean()) {
                    CompanyCourse companyCourse = new CompanyCourse();
                    companyCourse.setCompany(company);
                    companyCourse.setCourse(course);
                    companyCourseRepository.save(companyCourse);
                }
            }
        });

        // Create users with random roles and assign them to companies (excluding HDA)
        companies.forEach(company -> {
            if (!company.getCompanyEmailEnding().equals("HDA.com")) {
                int userCount = 0;
                while (userCount < 100) {
                    String name = NAMES.get(random.nextInt(NAMES.size()));
                    Role role = NON_ADMIN_ROLES.get(random.nextInt(NON_ADMIN_ROLES.size()));
                    String email = generateUniqueEmailForCompany(name, company);
                    Users user = createUserForCompany(company, name, email, role, "password");
                    enrollEmployee(company, user);
                    userCount++;
                }
            }
        });

        // Create specific test users and admins for company HDA
        Company companyHDA = companyRepository.findByCompanyEmailEnding("HDA.com").orElse(null);
        if (companyHDA != null) {
            // Create 20 Admins for HDA
            for (int i = 1; i <= 20; i++) {
                Users admin = createUserForCompany(companyHDA, "Admin" + i, "admin" + i + "@HDA.com", Role.Admin, "a");
                enrollEmployee(companyHDA, admin);
            }

            // Create specific test users for company HDA
            Users employee = createUserForCompany(companyHDA, "Employee", "e@HDA.com", Role.Employee, "a");
            Users teamLeader = createUserForCompany(companyHDA, "TeamLeader", "t@HDA.com", Role.TeamLeader, "a");
            Users admin = createUserForCompany(companyHDA, "Admin", "a@HDA.com", Role.Admin, "a");
            enrollEmployee(companyHDA, employee);
            enrollEmployee(companyHDA, teamLeader);
            enrollEmployee(companyHDA, admin);
        }

        // Assign random progress to users for their company's courses
        usersRepository.findAll().forEach(user -> {
            companyCourseRepository.findByCompany(user.getCompany()).forEach(companyCourse -> {
                if (random.nextBoolean()) {
                    UserProgress userProgress = new UserProgress();
                    userProgress.setUser(user);
                    userProgress.setCourse(companyCourse.getCourse());
                    userProgress.setProgress(random.nextInt(50)); // Random progress between 0 and 100
                    userProgressRepository.save(userProgress);
                }
            });
        });
    }

    private Company createCompany(String name, String emailEnding) {
        Company company = new Company();
        company.setName(name);
        company.setCompanyEmailEnding(emailEnding);
        company.setRegisterUser(0);
        company.setMaxUser(generateRandomMaxUser());
        return company;
    }

    private int generateRandomMaxUser() {
        Random random = new Random();
        return 100 + random.nextInt(150);
    }

    private Users createUserForCompany(Company company, String name, String email, Role role, String password) {
        Users user = new Users();
        user.setName(name);
        user.setCompanyEmail(email);
        user.setPassword(password);
        user.setRole(role);
        user.setCompany(company);
        return usersRepository.save(user);
    }

    private String generateUniqueEmailForCompany(String name, Company company) {
        String baseEmail = name.toLowerCase();
        String domain = company.getCompanyEmailEnding();
        String email;
        int count = 1;

        do {
            email = baseEmail + count + "@" + domain;
            count++;
        } while (usersRepository.findByCompanyEmailAndCompany(email, company).isPresent());

        return email;
    }

    private Course createCourse(String name, String description) {
        Course course = new Course();
        course.setName(name);
        course.setDescription(description);
        return course;
    }

    private void enrollEmployee(Company company, Users user) {
        EnrolledEmployee enrolledEmployee = new EnrolledEmployee();
        enrolledEmployee.setCompany(company);
        enrolledEmployee.setUser(user);
        enrolledEmployeeRepository.save(enrolledEmployee);

        company.setRegisterUser(company.getRegisterUser() + 1);
        companyRepository.save(company);
    }
}
