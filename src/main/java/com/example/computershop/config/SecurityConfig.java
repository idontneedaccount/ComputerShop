package com.example.computershop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final CustomAuthenticationProvider authenticationProvider;
    private final AuthenticationSuccessHandler successHandler;
    private final AuthenticationFailureHandler customAuthenticationFailureHandler;
    private final OAuth2SuccessHandler oauth2SuccessHandler;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private static final String LOGIN ="/auth/login";

    public SecurityConfig(CustomAuthenticationProvider authenticationProvider, 
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
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        authorize -> authorize
                                .requestMatchers("/auth/**", "/home", "/css/**",
                                        "/js/**", "/images/**", "/assets/**","/error/**","/assets2/**","/uploads/**",
                                        "/oauth2/**", "/login/oauth2/**","/user/shopping-page")
                                .permitAll()
                                .requestMatchers("/admin/**").hasRole("Admin")
                                .requestMatchers("/user/**","/cart/**")
                                .hasAnyAuthority("ROLE_User", "OIDC_USER","OAUTH2_USER")
                                .requestMatchers("/admin/product)","admin/categories")
                                .hasAnyAuthority("ROLE_Marketer", "OIDC_MARKETER","OAUTH2_MARKETER")
                                .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage(LOGIN)
                        .loginProcessingUrl("/auth/login-process")
                        .successHandler(successHandler)
                        .failureHandler(customAuthenticationFailureHandler)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl(LOGIN)
                        .permitAll())
                .authenticationProvider(authenticationProvider)
                .oauth2Login(oauth2 -> oauth2
                        .loginPage(LOGIN)
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
