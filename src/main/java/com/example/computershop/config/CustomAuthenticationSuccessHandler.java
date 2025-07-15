package com.example.computershop.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Qualifier("customAuthenticationSuccessHandler")
@Slf4j
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String username = authentication.getName();
        log.info("Authentication success for user: {}", username);
        
        // Log all authorities for debugging
        log.info("User {} authorities: {}", username, 
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList());
        
        // Check user roles and redirect accordingly
        for (GrantedAuthority auth : authentication.getAuthorities()) {
            String authority = auth.getAuthority();
            log.debug("Checking authority: {}", authority);
            
            if ("ROLE_Admin".equals(authority)) {
                log.info("Redirecting admin user {} to dashboard", username);
                response.sendRedirect("/admin/dashboard");
                return;
            }
            if ("ROLE_Customer".equals(authority)) {
                log.info("Redirecting user {} to shopping page", username);
                response.sendRedirect("/");
                return;
            }
            if ("ROLE_Sales".equals(authority)) {
                log.info("Redirecting sales user {} to dashboard", username);
                response.sendRedirect("/admin/dashboard");
                return;
            }
            if ("ROLE_Shipper".equals(authority)) {
                log.info("Redirecting shipper {} to shipper dashboard", username);
                response.sendRedirect("/shipper/dashboard");
                return;
            }
        }
        
        // Default redirect if no matching role found
        // This prevents users from getting stuck at /auth/login-process
        log.warn("No matching role found for user {}. Redirecting to default shopping page", username);
        response.sendRedirect("/user/shopping-page");
    }
}
