package com.example.backend.service;

import com.example.backend.repository.EnrolledEmployeeRepository;
import com.example.backend.util.LoggerUtil;
import com.example.backend.util.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

    @Autowired
    private EnrolledEmployeeRepository enrolledEmployeeRepository;

    public boolean isUserAuthorizedForCompany(UserInfo userInfo, Long companyId) {
        boolean isCorrectCompany = companyId.toString().equals(userInfo.getCompanyId());
        boolean hasValidRole = isUserEmployee(userInfo) || isUserTeamLeader(userInfo) || isUserAdmin(userInfo);
        boolean isEnrolled = isUserEnrolledInCompany(userInfo.getUserId(), companyId);

        LoggerUtil.log("Authorization check details:");
        LoggerUtil.log("User role: " + userInfo.getRole());
        LoggerUtil.log("isCorrectCompany: " + isCorrectCompany);
        LoggerUtil.log("hasValidRole: " + hasValidRole);
        LoggerUtil.log("isEnrolled: " + isEnrolled);

        return isCorrectCompany && hasValidRole && isEnrolled;
    }

    public boolean isUserEmployee(UserInfo userInfo) {
        LoggerUtil.log("Checking if user is Employee. User role: " + userInfo.getRole());
        return "EMPLOYEE".equalsIgnoreCase(userInfo.getRole());
    }

    public boolean isUserTeamLeader(UserInfo userInfo) {
        LoggerUtil.log("Checking if user is TeamLeader: " + userInfo);
        return "TEAMLEADER".equalsIgnoreCase(userInfo.getRole());
    }

    public boolean isUserAdmin(UserInfo userInfo) {
        LoggerUtil.log("Checking if user is Admin: " + userInfo);
        return "ADMIN".equalsIgnoreCase(userInfo.getRole());
    }

    private boolean isUserEnrolledInCompany(String keycloakUserId, Long companyId) {
        return enrolledEmployeeRepository.existsByKeycloakUserIdAndCompanyCompanyId(keycloakUserId, companyId);
    }

    public boolean isUserAuthorizedForUser(UserInfo userInfo, String userId) {
        return userInfo.getUserId().equals(userId) || isUserAdmin(userInfo);
    }
}
