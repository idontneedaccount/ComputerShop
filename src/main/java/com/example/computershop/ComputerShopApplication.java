package com.example.computershop;

import com.example.computershop.entity.User;
import com.example.computershop.enums.Role;
import com.example.computershop.repository.UserRepository;
import com.example.computershop.service.StorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;

@SpringBootApplication
public class ComputerShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(ComputerShopApplication.class, args);
    }

    @Bean
    CommandLineRunner init(StorageService storageService, UserRepository userRepository) {
        return args -> {
            storageService.init();

            // Tạo tài khoản admin mặc định nếu chưa có
            if (!userRepository.findByUsername("sales").isPresent()) {
                if (userRepository.findByUsername("sales").isEmpty()) {
                    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                    String encodedPassword = encoder.encode("sales");

                    User adminUser = User.builder()
                            .username("sales")
                            .password(encodedPassword)
                            .fullName("Sales")
                            .email("sales@gmail.com")
                            .phoneNumber("0123451526")
                            .address("Hà Nội")
                            .role(Role.Sales)
                            .isActive(true)
                            .isAccountLocked(false)
                            .provider("local")
                            .createdAt(LocalDateTime.now())
                            .build();

                    try {
                        userRepository.save(adminUser);
                    } catch (Exception e) {
                        System.err.println("❌ Lỗi khi tạo tài khoản admin: " + e.getMessage());
                    }
                } else {
                    System.out.println("ℹ️ Tài khoản admin đã tồn tại");
                }
            }
        };
    }
}
