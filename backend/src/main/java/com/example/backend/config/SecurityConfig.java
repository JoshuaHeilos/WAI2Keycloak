package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;


@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtConverter jwtConverter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .authorizeHttpRequests(authz -> authz
                        // User endpoints
                        .requestMatchers(HttpMethod.GET, "/api/users/**").hasAnyRole("EMPLOYEE", "TEAMLEADER", "ADMIN")

                        // Company endpoints
                        .requestMatchers(HttpMethod.GET, "/api/companies/**").hasAnyRole("EMPLOYEE", "TEAMLEADER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/companies/*/book-course").hasAnyRole("TEAMLEADER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/companies/*/delete-course").hasAnyRole("TEAMLEADER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/companies/*/update-max-users").hasRole("ADMIN")

                        // User-progress endpoints
                        .requestMatchers(HttpMethod.GET, "/api/user-progress/**").hasAnyRole("EMPLOYEE", "TEAMLEADER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/user-progress/**").hasAnyRole("EMPLOYEE", "TEAMLEADER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/user-progress/**").hasAnyRole("EMPLOYEE", "TEAMLEADER", "ADMIN")

                        // Course endpoints
                        .requestMatchers(HttpMethod.GET, "/api/courses/**").hasAnyRole("EMPLOYEE", "TEAMLEADER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/courses").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/courses/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/courses/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)));

        return http.build();
    }
}





/* OLD config
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeRequests(authz -> authz
                        .requestMatchers("/api/login", "/api/register", "/api/session-info", "/api/users/**",
                                "/api/companies/**", "/api/user-progress/**", "/api/courses/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/employee-dashboard")
                        .permitAll()
                )
                .logout(logout -> logout.permitAll())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                );

        return http.build();
    }

}
*/