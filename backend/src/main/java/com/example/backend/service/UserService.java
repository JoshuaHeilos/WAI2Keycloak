package com.example.backend.service;

import com.example.backend.model.Company;
import com.example.backend.model.Users;
import com.example.backend.repository.CompanyRepository;
import com.example.backend.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private CompanyRepository companyRepository;

    public Optional<Users> validateUser(String companyEmail, String password) {
        return usersRepository.findByCompanyEmailAndPassword(companyEmail, password);
    }

    public Optional<Users> findUserById(Long id) {
        return usersRepository.findById(id);
    }

    public void registerUser(Users user) {
        // Check if the email is already registered for the company
        String emailEnding = user.getCompanyEmail().split("@")[1];
        Company company = companyRepository.findByCompanyEmailEnding(emailEnding)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        Optional<Users> existingUser = usersRepository.findByCompanyEmailAndCompany(user.getCompanyEmail(), company);
        if (existingUser.isPresent()) {
            throw new RuntimeException("Email already registered for this company");
        }

        user.setCompany(company);
        usersRepository.save(user);
    }

    public void saveUser(Users user) {
        usersRepository.save(user);
    }
}
