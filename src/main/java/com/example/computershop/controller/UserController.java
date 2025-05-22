package com.example.computershop.controller;

import com.example.computershop.dto.request.UserCreationRequest;
import com.example.computershop.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
public class UserController {

    private static final String REGISTER = "register";
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new UserCreationRequest());
        return REGISTER;
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") @Valid UserCreationRequest request, BindingResult result, Model model) {

        if (result.hasErrors()) {
            return REGISTER;
        }

        try {
            userService.createUser(request);
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return REGISTER;
        }
    }

}
