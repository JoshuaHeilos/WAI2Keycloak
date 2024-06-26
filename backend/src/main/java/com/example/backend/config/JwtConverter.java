package com.example.backend.config;

import com.example.backend.util.LoggerUtil;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class JwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        // Add realm roles (priority)
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null) {
            List<String> realmRoles = (List<String>) realmAccess.get("roles");
            if (realmRoles != null) {
                for (String role : realmRoles) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
                }
            }
        }

        // Add resource roles (as fallback)
        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        if (resourceAccess != null) {
            Map<String, Object> account = (Map<String, Object>) resourceAccess.get("account");
            if (account != null) {
                List<String> roles = (List<String>) account.get("roles");
                if (roles != null) {
                    for (String role : roles) {
                        String mappedRole = mapResourceRole(role);
                        if (!authorities.contains(new SimpleGrantedAuthority("ROLE_" + mappedRole))) {
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + mappedRole));
                        }
                    }
                }
            }
        }

        // Add default role if no roles are present
        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_UNKNOWN"));
        }

        LoggerUtil.log("Extracted authorities: " + authorities);

        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }

    private String mapResourceRole(String resourceRole) {
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

