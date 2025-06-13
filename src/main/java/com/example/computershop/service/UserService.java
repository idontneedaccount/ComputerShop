package com.example.computershop.service;

import com.example.computershop.dto.request.UserCreateByAdmin;
import com.example.computershop.dto.request.UserUpdateByAdmin;
import com.example.computershop.entity.User;
import com.example.computershop.exception.AuthenticationException;
import com.example.computershop.repository.UserRepository;
import com.example.computershop.entity.Role;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;


@Service
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

    @Transactional
    public void createUserByAdmin(@NotNull UserCreateByAdmin request) {
        try {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new IllegalArgumentException("Tên đăng nhập đã được sử dụng!");
            }
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email đã được đăng ký!");
            }
            if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
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
            if (!user.getPhoneNumber().equals(userUpdateByAdmin.getPhoneNumber()) &&
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

