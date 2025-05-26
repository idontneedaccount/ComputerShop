package com.example.computershop.controller;

import lombok.Getter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("")
public class UserController {
    @GetMapping("/home")
    public String home() {
        return "home"; // Returns the home view
    }
}
