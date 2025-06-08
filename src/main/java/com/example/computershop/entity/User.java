package com.example.computershop.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "Users")
public class User implements UserDetails, OAuth2User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "userId" , columnDefinition = "nvarchar(255)")
    String userId;
    @Column(name = "username", unique = true , columnDefinition = "nvarchar(255)")
    String username;
    @Column(name = "password" , columnDefinition = "nvarchar(255)")
    String password;
    @Column(name = "fullName" , columnDefinition = "nvarchar(255)")
    String fullName;
    @Column(name = "email", unique = true , columnDefinition = "nvarchar(255)")
    String email;
    @Column(name = "phoneNumber", unique = true , columnDefinition = "nvarchar(255)")
    String phoneNumber;
    @Enumerated(EnumType.STRING)
    @Column(name = "role" , columnDefinition = "nvarchar(255)")
    Role role;
    @Column(name = "createdAt" , columnDefinition = "datetime")
    LocalDateTime createdAt;
    @Column(name = "isActive" , columnDefinition = "bit")
    boolean isActive;
    @Column(name = "verification_code" , columnDefinition = "nvarchar(255)")
    String verificationCode;
    @Column(name = "verification_expiration" , columnDefinition = "datetime")
    LocalDateTime verificationExpiration;
    @Column(name = "provider", columnDefinition = "nvarchar(255)")
    String provider;
    @Column(name = "address", columnDefinition = "nvarchar(255)")
    String address;
    @Transient
    private Map<String, Object> attributes;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> "ROLE_" + role.name());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return username;
    }
}
