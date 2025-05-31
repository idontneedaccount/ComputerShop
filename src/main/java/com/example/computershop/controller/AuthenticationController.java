package com.example.computershop.controller;

import com.example.computershop.dto.request.AuthenticationRequest;
import com.example.computershop.dto.request.UserCreationRequest;
import com.example.computershop.dto.request.VerifyUserRequest;
import com.example.computershop.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;
    static String register = "register";
    static String login = "login";
    static String errorAttr = "error";


    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new UserCreationRequest());
        return register;
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") @Valid UserCreationRequest request, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return register;
        }

        try {
            authenticationService.createUser(request);
            return "redirect:/auth/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute(errorAttr, e.getMessage());
            return register;
        }
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("user", new AuthenticationRequest());
        return login;
    }

    @PostMapping("/login")
    public String authenticate(@ModelAttribute("user") AuthenticationRequest request, Model model) {
        try {
            authenticationService.authenticate(request);
            return "redirect:/admin/home";
        } catch (IllegalArgumentException e) {
            model.addAttribute(errorAttr, e.getMessage());
            return login;
        }
    }

    @GetMapping("/verify")
    public String verifyUserByLink(@RequestParam("email") String email, @RequestParam("code") String code, Model model) {
        try {
            VerifyUserRequest verifyRequest = new VerifyUserRequest();
            verifyRequest.setEmail(email);
            verifyRequest.setVerificationCode(code);
            authenticationService.verifyUser(verifyRequest);
            return "redirect:/auth/login?verified=true";
        } catch (IllegalArgumentException e) {
            model.addAttribute(errorAttr, e.getMessage());
            return login;
        }
    }

    @GetMapping("/resend-verification")
    public String showResendVerificationForm(Model model) {
        model.addAttribute("email", "");
        return "resend-verification";
    }

    @PostMapping("/resend-verification")
    public String resendVerificationEmail(@RequestParam("email") String email, Model model) {
        try {
            authenticationService.resendVerificationEmail(email);
            return "redirect:/auth/login?resent=true";
        } catch (IllegalArgumentException e) {
            model.addAttribute(errorAttr, e.getMessage());
            return "resend-verification";
        }
    }
}
