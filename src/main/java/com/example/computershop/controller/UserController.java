package com.example.computershop.controller;


import com.example.computershop.entity.User;
import com.example.computershop.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@AllArgsConstructor
public class UserController {
    private UserService userService;

    private static final String USER = "user";
    @GetMapping("/home")
    public String home() {
        return "home"; // Returns the home view
    }

    @RequestMapping("/user")
    public String showUsers(Model model) {
        List<User> users = userService.getAll();
        model.addAttribute(USER, users);
        return "admin/user/user";
    }
}
