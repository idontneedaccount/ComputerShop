package com.example.computershop.service;

import com.example.computershop.dto.request.AuthenticationRequest;
import com.example.computershop.dto.request.UserCreationRequest;
import com.example.computershop.dto.request.VerifyUserRequest;
import com.example.computershop.entity.User;
import com.example.computershop.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    AuthenticationManager authenticationManager;
    EmailService emailService;

    public void     createUser(@NotNull UserCreationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered!");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number is already registered!");
        }
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new IllegalArgumentException("Passwords do not match!");
        }

        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode(request.getPassword());

        User user = User.builder()
                .username(request.getUsername())
                .password(encodedPassword)
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .role("User")
                .isActive(false)
                .createdAt(LocalDateTime.now())
                .build();
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationExpiration(LocalDateTime.now().plusMinutes(15));
        sendVerificationEmail(user);
        userRepository.save(user);
    }


    public void authenticate(@NotNull AuthenticationRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!user.isActive()) {
            throw new IllegalArgumentException("User is not active. Please verify your account.");
        }

        String usernameForAuth = user.getUsername();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(usernameForAuth, request.getPassword())
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid username or password");
        }

    }

    public void verifyUser(VerifyUserRequest verifyUserRequest) {
        Optional<User> optuser = userRepository.findByEmail(verifyUserRequest.getEmail());
        if (optuser.isPresent()) {
            User user = optuser.get();
            if (user.getVerificationExpiration().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Verification code has expired.");
            }
            user.setActive(true);
            user.setVerificationCode(null);
            user.setVerificationExpiration(null);
            userRepository.save(user);
        } else {
            throw new IllegalArgumentException("User not found.");
        }
    }

    public void resendVerificationEmail(String email) {
        Optional<User> optuser = userRepository.findByEmail(email);
        if (optuser.isPresent()) {
            User user = optuser.get();
            if (user.isActive()) {
                throw new IllegalArgumentException("User is already active.");
            }
            user.setVerificationCode(generateVerificationCode());
            user.setVerificationExpiration(LocalDateTime.now().plusHours(1));
            sendVerificationEmail(user);
            userRepository.save(user);
        } else {
            throw new IllegalArgumentException("User not found.");
        }
    }

    private void sendVerificationEmail(User user) {
        String subject = "Account Verification";
        String verificationCode = user.getVerificationCode();
        String verificationLink = "http://localhost:8080/auth/verify?email=" + user.getEmail() + "&code=" + verificationCode;

        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Welcome to our web!</h2>"
                + "<p style=\"font-size: 16px;\">Please click the button below to verify your account:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<a href=\"" + verificationLink + "\" style=\"display: inline-block; background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; font-weight: bold;\">Verify Account</a>"
                + "<p style=\"margin-top: 20px; font-size: 14px; color: #666;\">If the button doesn't work, you can also use this verification code: <span style=\"font-weight: bold;\">" + verificationCode + "</span></p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";

        try {
            emailService.sendEmail(user.getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private String generateVerificationCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }


}
