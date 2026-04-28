package com.mediverse.config;

import com.mediverse.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return userDetailsService;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(passwordEncoder());
        authProvider.setUserDetailsService(userDetailsService());
        return authProvider;
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return new CustomAuthenticationSuccessHandler();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                // Public pages
                .requestMatchers("/", "/home", "/about", "/services", "/contact", "/doctors", "/departments", "/branches").permitAll()
                .requestMatchers("/register", "/login", "/logout").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**", "/favicon.ico").permitAll()
                
                // Patient pages
                .requestMatchers("/patient/**").hasRole("PATIENT")
                
                // Doctor pages
                .requestMatchers("/doctor/**").hasRole("DOCTOR")
                
                // Admin pages
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // Any other request needs authentication
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(successHandler())
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .clearAuthentication(true)
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            .rememberMe(remember -> remember
                .key("mediverse-remember-me")
                .tokenValiditySeconds(86400) // 24 hours
                .userDetailsService(userDetailsService())
            )
            .csrf(csrf -> csrf
                // Enable CSRF protection for all state-changing operations
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                // Allow form submissions from the same origin
                .ignoringRequestMatchers(
                    "/api/**"  // Exclude API endpoints if needed
                )
            );

        return http.build();
    }
}
