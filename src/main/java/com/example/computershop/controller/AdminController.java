package com.example.computershop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AdminController {
    @RequestMapping("/admin/dashboard")
    public String showAdminPage(){
        return "admin/dashboard";
    }
    @RequestMapping("/user/shopping-page")
    public String showShoppingPage(){ return "user/shoppingpage";}
    @RequestMapping("/home")
    public String showProfilePage() {
        return "home";
    }
}
