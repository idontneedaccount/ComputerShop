package com.example.computershop.config;

import com.example.computershop.entity.Role;
import com.example.computershop.entity.User;
import com.example.computershop.repository.UserRepository;
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
            Authentication authentication) throws IOException {
        try {
            if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
                String provider = oauthToken.getAuthorizedClientRegistrationId();
                OAuth2User oauth2User = oauthToken.getPrincipal();
                Map<String, Object> attributes = oauth2User.getAttributes();
                
                String email = getEmailFromAttributes(provider, attributes);
                if (email == null) {
                    getRedirectStrategy().sendRedirect(request, response, "/auth/login?oauth2error=email_not_found");
                    return;
                }

                String name = getNameFromAttributes(provider, attributes);
                Optional<User> existingUser = userRepository.findByEmail(email);

                if (existingUser.isPresent()) {
                    User user = existingUser.get();
                    if (user.getProvider() == null || user.getProvider().equalsIgnoreCase("local")) {
                        getRedirectStrategy().sendRedirect(request, response, "/auth/login?oauth2error=provider_conflict");
                        return;
                    }
                    // Allow login if provider matches
                    if (!user.getProvider().equalsIgnoreCase(provider)) {
                        getRedirectStrategy().sendRedirect(request, response, "/auth/login?oauth2error=provider_mismatch");
                        return;
                    }
                } else {
                    // Create new user with the provider
                    User newUser = User.builder()
                            .email(email)
                            .username(email)
                            .password("")
                            .role(Role.USER)
                            .isActive(true)
                            .fullName(name != null ? name : email)
                            .createdAt(LocalDateTime.now())
                            .provider(provider)
                            .address("")
                            .build();
                    userRepository.save(newUser);
                }
            }
            String targetUrl = "/home";
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        } catch (Exception e) {
            getRedirectStrategy().sendRedirect(request, response, "/auth/login?oauth2error=oauth2_error");
        }
    }

    private String getEmailFromAttributes(String provider, Map<String, Object> attributes) {
        try {
            if ("github".equals(provider)) {
                // GitHub có thể trả về email trong nhiều trường khác nhau
                String email = (String) attributes.get("email");
                if (email == null) {
                    // Sử dụng username để tạo email nếu không có email
                    String username = (String) attributes.get("login");
                    if (username != null) {
                        email = username + "@github.com";
                    }
                }
                return email;
            } else if ("facebook".equals(provider)) {
                return (String) attributes.get("email");
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getNameFromAttributes(String provider, Map<String, Object> attributes) {
        try {
            if ("github".equals(provider)) {
                String name = (String) attributes.get("name");
                if (name == null) {
                    name = (String) attributes.get("login");
                }
                return name;
            } else if ("facebook".equals(provider)) {
                return (String) attributes.get("name");
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}