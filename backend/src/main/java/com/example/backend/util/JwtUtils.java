package com.example.backend.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

public class JwtUtils {

    public static UserInfo extractUserInfo(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String userId = jwt.getSubject();
        String companyId = jwt.getClaimAsString("companyId");

        // Extract role from realm_access
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        List<String> roles = (List<String>) realmAccess.get("roles");
        String role = roles != null && !roles.isEmpty() ? roles.get(0) : "UNKNOWN";

        LoggerUtil.log("Extracted role from JWT: " + role);

        return new UserInfo(userId, companyId, role);
    }

    private static String extractRole(Jwt jwt) {
        // Add realm roles (priority)
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null) {
            List<String> realmRoles = (List<String>) realmAccess.get("roles");
            if (realmRoles != null && !realmRoles.isEmpty()) {
                return realmRoles.get(0);
            }
        }

        // Fallback to resource roles if realm roles are not present
        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        if (resourceAccess != null) {
            Map<String, Object> account = (Map<String, Object>) resourceAccess.get("account");
            if (account != null) {
                List<String> roles = (List<String>) account.get("roles");
                if (roles != null && !roles.isEmpty()) {
                    return mapResourceRole(roles.get(0));
                }
            }
        }

        // Default role if no roles are found
        LoggerUtil.log("No roles found in JWT, using default role");
        return "UNKNOWN";
    }

    private static String mapResourceRole(String resourceRole) {
        switch (resourceRole) {
            case "manage-account":
            case "manage-account-links":
            case "view-profile":
                return "EMPLOYEE";
            default:
                return resourceRole.toUpperCase();
        }
    }
}