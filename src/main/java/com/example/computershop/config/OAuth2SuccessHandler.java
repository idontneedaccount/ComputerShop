package com.example.computershop.config;

import com.example.computershop.entity.User;
import com.example.computershop.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Component
@Qualifier("oauth2SuccessHandler")
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UserRepository userRepository;

    @Autowired
    public OAuth2SuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        try {
            if (authentication instanceof OAuth2AuthenticationToken) {
                OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
                OAuth2User oauth2User = oauthToken.getPrincipal();
                Map<String, Object> attributes = oauth2User.getAttributes();
                String email = getEmailFromAttributes(attributes);
                if (email == null) {
                    getRedirectStrategy().sendRedirect(request, response, "/auth/login?oauth2error=email_not_found");
                    return;
                }
                String name = getNameFromAttributes(attributes);
                Optional<User> existingUser = userRepository.findByEmail(email);
                if (existingUser.isPresent()) {
                    User user = existingUser.get();
                    if (user.getProvider() == null || user.getProvider().equalsIgnoreCase("local")) {
                        // Nếu tài khoản đã tạo bằng local, không cho đăng nhập Google
                        getRedirectStrategy().sendRedirect(request, response, "/auth/login?oauth2error=provider_conflict");
                        return;
                    }
                    // Nếu provider là google, cho đăng nhập bình thường
                } else {
                    // Tạo user mới với provider = google
                    User newUser = User.builder()
                            .email(email)
                            .username(email)
                            .password("")
                            .role("User")
                            .isActive(true)
                            .fullName(name != null ? name : email)
                            .createdAt(LocalDateTime.now())
                            .provider("google")
                            .build();
                    userRepository.save(newUser);
                }
            }
            getRedirectStrategy().sendRedirect(request, response, "/home");
        } catch (Exception e) {
            getRedirectStrategy().sendRedirect(request, response, "/auth/login?oauth2error=oauth2_error");
        }
    }

    private String getEmailFromAttributes(Map<String, Object> attributes) {
        try {
            String email = (String) attributes.get("email");
            if (email == null) {
                email = (String) attributes.get("sub");
            }
            return email;
        } catch (Exception e) {
            return null;
        }
    }

    private String getNameFromAttributes(Map<String, Object> attributes) {
        try {
            String name = (String) attributes.get("name");
            if (name == null) {
                name = (String) attributes.get("given_name");
            }
            return name;
        } catch (Exception e) {
            return null;
        }
    }
}