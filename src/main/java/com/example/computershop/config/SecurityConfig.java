package com.example.computershop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final AuthenticationProvider authenticationProvider;
    private final AuthenticationSuccessHandler successHandler;
    private final AuthenticationFailureHandler customAuthenticationFailureHandler;
    private final OAuth2SuccessHandler oauth2SuccessHandler;
    private final ClientRegistrationRepository clientRegistrationRepository;

    public SecurityConfig(AuthenticationProvider authenticationProvider, 
                         @Qualifier("customAuthenticationSuccessHandler") AuthenticationSuccessHandler successHandler, 
                         AuthenticationFailureHandler customAuthenticationFailureHandler,
                         @Qualifier("oauth2SuccessHandler") OAuth2SuccessHandler oauth2SuccessHandler,
                         ClientRegistrationRepository clientRegistrationRepository) {
        this.authenticationProvider = authenticationProvider;
        this.successHandler = successHandler;
        this.customAuthenticationFailureHandler = customAuthenticationFailureHandler;
        this.oauth2SuccessHandler = oauth2SuccessHandler;
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, OAuth2FailureHandler oauth2FailureHandler) throws Exception {
        http
                .csrf((csrf) -> csrf.disable())
                .authorizeHttpRequests(
                        authorize -> authorize
                                .requestMatchers("/auth/**", "/home", "/css/**",
                                        "/js/**", "/images/**", "/assets/**","/cart/**","/error/**","/assets2/**","/uploads/**",
                                        "/oauth2/**", "/login/oauth2/**")
                                .permitAll()
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                .requestMatchers("/user/**").hasRole("USER")
                                .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login-process")
                        .successHandler(successHandler)
                        .failureHandler(customAuthenticationFailureHandler)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/auth/login")
                        .permitAll())
                .authenticationProvider(authenticationProvider)
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/auth/login")
                        .successHandler(oauth2SuccessHandler)
                        .failureHandler(oauth2FailureHandler)
                        .clientRegistrationRepository(clientRegistrationRepository)
                        .authorizationEndpoint(authorization -> authorization
                                .baseUri("/oauth2/authorization"))
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/auth/login/oauth2/code/*"))
                );
        return http.build();
    }
}
