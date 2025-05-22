package com.example.computershop.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "userId")
    String userId;
    @Column(name = "username", unique = true)
    String username;
    @Column(name = "password")
    String password;
    @Column(name = "fullName")
    String fullName;
    @Column(name = "email", unique = true)
    String email;
    @Column(name = "phoneNumber", unique = true)
    String phoneNumber;
    @Column(name = "role")
    String role;
    @Column(name = "createdAt")
    LocalDateTime createdAt;
    @Column(name = "isActive")
    boolean isActive;
    
}
