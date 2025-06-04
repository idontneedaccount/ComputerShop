package com.example.computershop.controller;

import com.example.computershop.entity.User;
import com.example.computershop.repository.UserRepository;
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
<<<<<<< HEAD
    private UserService userService;
    private UserRepository userRepository;
    private static final String USER2="redirect:/admin/user";
    private static final String USER_VIEW= "admin/user/user";
    private static final String USER = "user";
=======

>>>>>>> ba7f7168e3fa4a0216bdfe36122b74359f7ffee8
    @GetMapping("/home")
    public String home() {
        return "home";
    }


    @RequestMapping("/user")
    public String showUsers(Model model) {
        List<User> users = userService.getAll();
        model.addAttribute(USER, users);
        return USER_VIEW;
    }
    @RequestMapping("/add-user")
    public String addUser(Model model) {
        User user = new User();
        model.addAttribute(USER, user);
        return "admin/user/add";
    }

    @PostMapping("/add-user")
    public String addUser(@ModelAttribute(USER) User user) {
        if (Boolean.TRUE.equals(this.userService.create(user))) {
            return USER2;
        } else {
            return "admin/user/add";
        }
    }
    @GetMapping("/edit-user/{userId}")
    public String editUser(Model model, @PathVariable("userId") String userId) {
        User user = this.userRepository.findById(userId).orElse(null);
        model.addAttribute(USER, user);
        return "admin/user/edit";
    }
    @PostMapping("/edit-user")
    public String updateUser(@ModelAttribute(USER) User user) {
        if (Boolean.TRUE.equals(this.userService.update(user))) {
            return USER2;
        } else  {
            return "admin/user/edit";
        }
    }
    @GetMapping("/delete-user/{userId}")
    public String deleteUser(@PathVariable("userId") String userId) {
        if (Boolean.TRUE.equals(this.userService.delete(userId))) {
            return USER2;
        } else {
            return USER_VIEW;
        }
    }
}
