package com.example.computershop;

import com.example.computershop.enums.Role;
import com.example.computershop.entity.User;
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
            if (userRepository.findByUsername("admin").isEmpty()) {
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                String encodedPassword = encoder.encode("Admin123!");

                User adminUser = User.builder()
                        .username("admin")
                        .password(encodedPassword)
                        .fullName("Administrator")
                        .email("admin@computershop.com")
                        .phoneNumber("0123456789")
                        .address("Hà Nội")
                        .role(Role.Admin)
                        .isActive(true)
                        .isAccountLocked(false)
                        .provider("local")
                        .createdAt(LocalDateTime.now())
                        .build();

                try {
                    userRepository.save(adminUser);
                    System.out.println("✅ Tài khoản admin mặc định đã được tạo:");
                    System.out.println("   Username: admin");
                    System.out.println("   Password: Admin123!");
                } catch (Exception e) {
                    System.err.println("❌ Lỗi khi tạo tài khoản admin: " + e.getMessage());
                }
            } else {
                System.out.println("ℹ️ Tài khoản admin đã tồn tại");
            }
        };
    }
}
