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
}
