package com.example.computershop.controller;

import com.example.computershop.dto.request.UserCreationRequest;
import com.example.computershop.dto.request.VerifyUserRequest;
import com.example.computershop.exception.AuthenticationException;
import com.example.computershop.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;
    static String register = "auth/register";
    static String login = "auth/login";
    static String resendVerification = "auth/resendVerification";
    static String manualVerify = "auth/manual-verify";
    static String errorAttr = "error";
    static String messageAttr = "message";
    static String redirectlogin = "redirect:/auth/login";
    static String verifyAttr = "verifyRequest";
    static String forgotPassword = "auth/forgot-password";
    static String resetPassword = "auth/reset-password";
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new UserCreationRequest());
        return register;
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserCreationRequest request, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute(errorAttr, "Vui lòng kiểm tra lại thông tin đăng ký.");
            return register;
        }
        try {
            authenticationService.createUser(request);
            model.addAttribute(messageAttr, "Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản.");
            return redirectlogin;
        } catch (AuthenticationException e) {
            model.addAttribute(errorAttr, e.getMessage());
            return register;
        }
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute(errorAttr, error);
        }
        return login;
    }

    @GetMapping("/verify")
    public String verifyUserByLink(@RequestParam("email") String email, @RequestParam("code") String code, Model model) {
        VerifyUserRequest verifyRequest = new VerifyUserRequest();
        verifyRequest.setEmail(email);
        verifyRequest.setVerificationCode(code);
        
        try {
            if (authenticationService.isUserActive(email)) {
                model.addAttribute(messageAttr, "Tài khoản của bạn đã được kích hoạt. Bạn có thể đăng nhập ngay.");
                return redirectlogin;
            }

            authenticationService.verifyUser(verifyRequest);
            model.addAttribute(messageAttr, "Tài khoản đã xác thực thành công. Bạn có thể đăng nhập.");
            return "redirect:/auth/login?verified=true";
        } catch (AuthenticationException e) {
            model.addAttribute(errorAttr, e.getMessage());
            return login;
        }
    }

    @GetMapping("/resend-verification")
    public String showResendVerificationForm(Model model) {
        if (!model.containsAttribute(verifyAttr)) {
            model.addAttribute(verifyAttr, new VerifyUserRequest());
        }
        return resendVerification;
    }

    @PostMapping("/resend-verification")
    public String resendVerificationEmail(
            @Valid @ModelAttribute("verifyRequest") VerifyUserRequest request,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute(errorAttr, "Vui lòng kiểm tra lại thông tin.");
            return resendVerification;
        }
        try {
            if (authenticationService.isUserActive(request.getEmail())) {
                model.addAttribute(messageAttr, "Tài khoản của bạn đã được kích hoạt. Bạn có thể đăng nhập ngay.");
                return login;
            }
            authenticationService.resendVerificationEmail(request.getEmail());
            model.addAttribute(messageAttr, "Đã gửi lại email xác thực. Vui lòng kiểm tra email của bạn.");
            return login;
        } catch (AuthenticationException e) {
            model.addAttribute(errorAttr, e.getMessage());
            return resendVerification;
        }
    }

    @GetMapping("/manual-verify")
    public String showManualVerifyForm(Model model) {
        model.addAttribute(verifyAttr, new VerifyUserRequest());
        return manualVerify;
    }

    @PostMapping("/manual-verify")
    public String verifyUserManually(@Valid @ModelAttribute("verifyRequest") VerifyUserRequest request,
                                   BindingResult result,
                                   Model model) {
        if (result.hasErrors()) {
            model.addAttribute(errorAttr, "Vui lòng kiểm tra lại thông tin xác thực.");
            return manualVerify;
        }

        try {
            if (authenticationService.isUserActive(request.getEmail())) {
                model.addAttribute(messageAttr, "Tài khoản của bạn đã được kích hoạt. Bạn có thể đăng nhập ngay.");
                return redirectlogin;
            }

            authenticationService.verifyUser(request);
            model.addAttribute(messageAttr, "Tài khoản đã xác thực thành công. Bạn có thể đăng nhập.");
            return "redirect:/auth/login?verified=true";
        } catch (AuthenticationException e) {
            model.addAttribute(errorAttr, e.getMessage());
            return manualVerify;
        }
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        return forgotPassword;
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String username, RedirectAttributes redirectAttributes) {
        try {
            authenticationService.sendPasswordResetEmail(username);
            redirectAttributes.addFlashAttribute(messageAttr, "Đã gửi email hướng dẫn đặt lại mật khẩu. Vui lòng kiểm tra email của bạn.");
            return redirectlogin;
        } catch (AuthenticationException e) {
            redirectAttributes.addFlashAttribute(errorAttr, e.getMessage());
            return redirectlogin;
        }
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam String email, @RequestParam String token, Model model) {
        try {
            if (!authenticationService.validatePasswordResetToken(email, token)) {
                model.addAttribute(errorAttr, "Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.");
                return redirectlogin;
            }
            model.addAttribute("email", email);
            model.addAttribute("token", token);
            return resetPassword;
        } catch (AuthenticationException e) {
            model.addAttribute(errorAttr, e.getMessage());
            return redirectlogin;
        }
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String email,
                                     @RequestParam String token,
                                     @RequestParam String password,
                                     @RequestParam String passwordConfirm,
                                     Model model) {
        try {
            if (!password.equals(passwordConfirm)) {
                model.addAttribute(errorAttr, "Mật khẩu xác nhận không khớp!");
                return resetPassword;
            }
            authenticationService.resetPassword(email, token, password);
            model.addAttribute(messageAttr, "Đặt lại mật khẩu thành công. Bạn có thể đăng nhập với mật khẩu mới.");
            return redirectlogin;
        } catch (AuthenticationException e) {
            model.addAttribute(errorAttr, e.getMessage());
            return resetPassword;
        }
    }
}
