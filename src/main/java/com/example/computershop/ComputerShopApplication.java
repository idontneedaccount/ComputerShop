package com.example.computershop;

import com.example.computershop.dto.request.UserCreationRequest;
import com.example.computershop.service.StorageService;
import com.example.computershop.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ComputerShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(ComputerShopApplication.class, args);
    }
    @Bean
    CommandLineRunner init(UserService userService, StorageService storageService) {
        return args -> storageService.init();
    }
}
