package com.example.computershop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
        private final AuthenticationProvider authenticationProvider;
        private final AuthenticationSuccessHandler successHandler;

        public SecurityConfig(AuthenticationProvider authenticationProvider, AuthenticationSuccessHandler successHandler) {
                this.authenticationProvider = authenticationProvider;
                this.successHandler = successHandler;
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(
                                                authorize -> authorize
                                                                .requestMatchers("/auth/**", "/home", "/css/**",
                                                                                "/js/**", "/images/**", "/assets/**")
                                                                .permitAll()
                                                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                                                .requestMatchers("/user/**").hasRole("USER")
                                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/auth/login")
                                                .loginProcessingUrl("/auth/login-process")
                                                .successHandler(successHandler)
                                                .failureUrl("/auth/login?error=true")
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/auth/login")
                                                .permitAll())
                                .authenticationProvider(authenticationProvider);
                return http.build();
        }


}
