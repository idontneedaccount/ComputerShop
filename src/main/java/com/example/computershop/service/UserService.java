package com.example.computershop.service;

import com.example.computershop.dto.request.UserCreateByAdmin;
import com.example.computershop.dto.request.UserUpdateByAdmin;
import com.example.computershop.dto.UserProfileData;
import com.example.computershop.entity.User;
import com.example.computershop.repository.UserRepository;
import com.example.computershop.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;


@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    /**
     * Lấy thông tin user hiện tại từ Security Context
     * Xử lý cả OAuth2 và Form Authentication
     * @return UserProfileData chứa user và metadata
     */
    public UserProfileData getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || 
                authentication.getName().equals("anonymousUser")) {
                throw new IllegalStateException("Người dùng chưa đăng nhập");
            }

            if (authentication instanceof OAuth2AuthenticationToken) {
                return handleOAuth2Authentication((OAuth2AuthenticationToken) authentication);
            } else {
                return handleFormAuthentication(authentication);
            }
            
        } catch (Exception e) {
            log.error("Error getting current user", e);
            throw new IllegalStateException("Không thể lấy thông tin người dùng: " + e.getMessage(), e);
        }
    }
    
    private UserProfileData handleOAuth2Authentication(OAuth2AuthenticationToken oauth2Token) {
        OAuth2User oauth2User = oauth2Token.getPrincipal();
        String provider = oauth2Token.getAuthorizedClientRegistrationId();

        String email;
        String realUsername = null;
        
        if ("github".equals(provider)) {
            String directEmail = (String) oauth2User.getAttributes().get("email");
            String githubUsername = (String) oauth2User.getAttributes().get("login");
            
            if (directEmail != null && !directEmail.isEmpty()) {
                email = directEmail;
                realUsername = githubUsername;
            } else {
                email = githubUsername + "@github.com";
                realUsername = githubUsername;
            }
        } else {
            email = (String) oauth2User.getAttributes().get("email");
            if (email == null) {
                throw new IllegalStateException("Không thể lấy email từ " + provider + " OAuth2");
            }}
        
        // Tìm user trong database bằng email (vì OAuth2 user có username = email)
        User user = userRepository.findByUsername(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng OAuth2: " + email));
        
        return UserProfileData.builder()
                .user(user)
                .isOAuth2(true)
                .authProvider(provider)
                .oauth2Attributes(oauth2User.getAttributes())
                .oauth2Authorities(oauth2User.getAuthorities())
                .realUsername(realUsername)
                .build();
    }
    
    private UserProfileData handleFormAuthentication(Authentication authentication) {
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng: " + username));
        
        return UserProfileData.builder()
                .user(user)
                .isOAuth2(false)
                .authProvider("local")
                .build();
    }

    @Transactional
    public void createUserByAdmin(@NotNull UserCreateByAdmin request) {
        try {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new IllegalArgumentException("Tên đăng nhập đã được sử dụng!");
            }
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email đã được đăng ký!");
            }
            if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty() && 
                userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new IllegalArgumentException("Số điện thoại đã được đăng ký!");
            }
    
            PasswordEncoder encoder = new BCryptPasswordEncoder();
            String encodedPassword = encoder.encode(request.getPassword());
    
            User user = User.builder()
                    .username(request.getUsername())
                    .password(encodedPassword)
                    .fullName(request.getFullName())
                    .email(request.getEmail())
                    .phoneNumber(request.getPhoneNumber())
                    .role(Role.valueOf(request.getRole().toUpperCase()))
                    .isActive(request.getActive())
                    .createdAt(LocalDateTime.now())
                    .address(request.getAddress())
                    .provider("local")
                    .isAccountLocked(false)
                    .build();
    
            userRepository.save(user);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Lỗi khi tạo người dùng: " + e.getMessage(), e);
        }
    }


    @Transactional
    public Boolean delete(String userId) {
        try {
            userRepository.deleteById(userId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    public void updateByAdmin(@NotNull UserUpdateByAdmin userUpdateByAdmin) {
        try {
            User user = userRepository.findById(userUpdateByAdmin.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

            if (!user.getUsername().equals(userUpdateByAdmin.getUsername()) &&
                    userRepository.existsByUsername(userUpdateByAdmin.getUsername())) {
                throw new IllegalArgumentException("Tên đăng nhập đã tồn tại trong hệ thống");
            }
            if (!user.getEmail().equals(userUpdateByAdmin.getEmail()) &&
                    userRepository.existsByEmail(userUpdateByAdmin.getEmail())) {
                throw new IllegalArgumentException("Email đã được sử dụng bởi tài khoản khác");
            }
            if (userUpdateByAdmin.getPhoneNumber() != null && !userUpdateByAdmin.getPhoneNumber().isEmpty() &&
                !userUpdateByAdmin.getPhoneNumber().equals(user.getPhoneNumber()) &&
                userRepository.existsByPhoneNumber(userUpdateByAdmin.getPhoneNumber())) {
                throw new IllegalArgumentException("Số điện thoại đã được sử dụng bởi tài khoản khác");
            }

            user.setUsername(userUpdateByAdmin.getUsername());
            user.setFullName(userUpdateByAdmin.getFullName());
            user.setEmail(userUpdateByAdmin.getEmail());
            user.setPhoneNumber(userUpdateByAdmin.getPhoneNumber());
            user.setAddress(userUpdateByAdmin.getAddress());
            user.setRole(userUpdateByAdmin.getRole());
            user.setActive(userUpdateByAdmin.getActive());
            user.setIsAccountLocked(userUpdateByAdmin.getIsAccountLocked());

            if (userUpdateByAdmin.getPassword() != null && !userUpdateByAdmin.getPassword().isBlank()) {
                user.setPassword(passwordEncoder.encode(userUpdateByAdmin.getPassword()));
            }

            userRepository.save(user);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Lỗi khi tạo người dùng: " + e.getMessage(), e);
        }
    }
}

