package com.example.computershop.config;

import com.example.computershop.entity.Role;
import com.example.computershop.entity.User;
import com.example.computershop.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
                    redirectWithError(request, response, "email_not_found");
                    return;
                }

                String name = getNameFromAttributes(provider, attributes);
                Optional<User> existingUser = userRepository.findByEmail(email);

                User user;
                if (existingUser.isPresent()) {
                    user = existingUser.get();

                    // ⚠️ Nếu đã tồn tại mà provider là local, từ chối
                    if (user.getProvider() == null || user.getProvider().equalsIgnoreCase("local")) {
                        redirectWithError(request, response, "provider_conflict");
                        return;
                    }

                    // ⚠️ Nếu provider không khớp, từ chối
                    if (!user.getProvider().equalsIgnoreCase(provider)) {
                        redirectWithError(request, response, "provider_mismatch");
                        return;
                    }
                } else {
                    // Tạo user mới
                    String username = generateUniqueUsername(email, provider);
                    String uniquePhoneNumber = generateUniquePhoneNumber(provider);
                    user = User.builder()
                            .email(email)
                            .username(username)
                            .password("")
                            .role(Role.Customer) // mặc định là Customer;
                            .isActive(true)
                            .fullName(name != null ? name : email)
                            .createdAt(LocalDateTime.now())
                            .provider(provider)
                            .phoneNumber(uniquePhoneNumber)
                            .address("")
                            .isAccountLocked(false)
                            .build();
                    userRepository.save(user);
                }

                // ✅ GÁN QUYỀN CHO NGƯỜI DÙNG DỰA TRÊN ROLE
                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
                
                // ✅ Debug để chắc chắn
                authorities.forEach(auth -> System.out.println(">> GrantedAuthority: " + auth.getAuthority()));

                // ✅ Tạo user mới có quyền cập nhật
                OAuth2User newOauth2User = new DefaultOAuth2User(
                        authorities,
                        oauth2User.getAttributes(),
                        "sub" // hoặc "email" nếu Google không có "sub"
                );

                OAuth2AuthenticationToken newAuth = new OAuth2AuthenticationToken(
                        newOauth2User,
                        authorities,
                        oauthToken.getAuthorizedClientRegistrationId()
                );

                // ✅ Gán lại vào SecurityContext
                SecurityContextHolder.getContext().setAuthentication(newAuth);
                
                // ✅ Redirect based on user role
                String redirectUrl = determineRedirectUrl(user);
                getRedirectStrategy().sendRedirect(request, response, redirectUrl);
            } else {
                // Fallback for non-OAuth2 authentication
                getRedirectStrategy().sendRedirect(request, response, "/user/shopping-page");
            }

        } catch (Exception e) {
            redirectWithError(request, response, "oauth2_error");
        }
    }

    private void redirectWithError(HttpServletRequest request, HttpServletResponse response, String errorMessage) throws IOException {
        String encodedError = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        getRedirectStrategy().sendRedirect(request, response, "/auth/login?oauth2error=" + encodedError);
    }

    private String getEmailFromAttributes(String provider, Map<String, Object> attributes) {
        try {
            if ("google".equals(provider)) {
                return (String) attributes.get("email");
            } else if ("github".equals(provider)) {
                String email = (String) attributes.get("email");
                if (email == null) {
                    String username = (String) attributes.get("login");
                    if (username != null) {
                        email = username + "@github.com";
                    }
                }
                return email;
            } else if ("facebook".equals(provider)) {
                String email = (String) attributes.get("email");
                if (email == null) {
                    String facebookId = (String) attributes.get("id");
                    if (facebookId != null) {
                        email = "facebook_" + facebookId + "@facebook.com";
                    }
                }
                return email;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getNameFromAttributes(String provider, Map<String, Object> attributes) {
        try {
            if ("google".equals(provider)) {
                return (String) attributes.get("name");
            } else if ("github".equals(provider)) {
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

    private String generateUniqueUsername(String email, String provider) {
        if (email != null && !email.isEmpty()) {
            if (!userRepository.existsByUsername(email)) {
                return email;
            }
        }

        String baseUsername = provider + "_user";
        String username = baseUsername;
        int counter = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + "_" + counter;
            counter++;
        }

        return username;
    }

    private String generateUniquePhoneNumber(String provider) {
        long timestamp = System.currentTimeMillis();
        String basePhoneNumber = provider + "_" + timestamp;
        String phoneNumber = basePhoneNumber;
        int counter = 1;

        while (userRepository.existsByPhoneNumber(phoneNumber)) {
            phoneNumber = basePhoneNumber + "_" + counter;
            counter++;
        }

        return phoneNumber;
    }
    
    private String determineRedirectUrl(User user) {
        Role userRole = user.getRole();
        
        switch (userRole) {
            case Admin:
                return "/admin/dashboard";
            case Customer:
                return "/";
            case Shipper:
                return "/admin/dashboard";
            case Sales:
                return "/admin/dashboard";
            default:
                return "/user/shopping-page";
        }
    }
}
