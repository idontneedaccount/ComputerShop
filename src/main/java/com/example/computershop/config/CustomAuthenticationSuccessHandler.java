package com.example.computershop.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Qualifier;

@Component
@Qualifier("customAuthenticationSuccessHandler")
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        for (GrantedAuthority auth : authentication.getAuthorities()) {
            if ("ROLE_ADMIN".equals(auth.getAuthority())) {
                response.sendRedirect("/admin/dashboard");
                return;
            }
            if ("ROLE_USER".equals(auth.getAuthority())) {
                response.sendRedirect("/user/shopping-page");
                return;
            }
            if("ROLE_USER".equals(auth.getAuthority())){
                response.sendRedirect("/user/shopping-page");
                return;
            }
            if("ROLE_USER".equals(auth.getAuthority())){
                response.sendRedirect("/user/shopping-page");
                return;
            }
        }
    }
}
