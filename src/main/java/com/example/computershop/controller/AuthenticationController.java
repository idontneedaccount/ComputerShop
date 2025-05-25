package com.example.computershop.controller;

import com.example.computershop.dto.request.AuthenticationRequest;
import com.example.computershop.dto.request.UserCreationRequest;
import com.example.computershop.service.AuthenticationService;
import com.example.computershop.service.UserService;
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
    UserService userService;
    static String register = "register";
    static String login = "login";
    static String error = "error";


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
            userService.createUser(request);
            return "redirect:/auth/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute(error, e.getMessage());
            return register;
        }
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("authenticationRequest", new AuthenticationRequest());
        return login;
    }

    @PostMapping("/login")
    public String authenticate(@ModelAttribute("authenticationRequest") AuthenticationRequest request, Model model) {
        try {
            boolean result = authenticationService.authenticate(request);
            if (result) {
                return "redirect:/home";
            } else {
                model.addAttribute(error, "Invalid username or password");
                return login;
            }
        } catch (IllegalArgumentException e) {
            model.addAttribute(error, e.getMessage());
            return login;
        }
    }
}
