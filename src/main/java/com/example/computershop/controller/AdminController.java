package com.example.computershop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AdminController {
    @RequestMapping("/admin/dashboard")
    public String showAdminPage(){
        return "admin/dashboard";
    }
}
