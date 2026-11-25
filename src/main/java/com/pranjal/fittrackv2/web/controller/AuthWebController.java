package com.pranjal.fittrackv2.web.controller;

import com.pranjal.fittrackv2.dto.RegisterRequest;
import com.pranjal.fittrackv2.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthWebController {

    private final AuthService authService;

    @GetMapping("/")
    public String home(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        model.addAttribute("fullName", "");
        model.addAttribute("email", "");
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerSubmit(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model,
            RedirectAttributes redirectAttributes) {

        model.addAttribute("fullName", fullName);
        model.addAttribute("email", email);

        if (fullName == null || fullName.trim().length() < 2) {
            model.addAttribute("error", "Full name must be at least 2 characters.");
            return "auth/register";
        }
        if (email == null || !email.contains("@")) {
            model.addAttribute("error", "Please enter a valid email address.");
            return "auth/register";
        }
        if (password == null || password.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters long.");
            return "auth/register";
        }
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            return "auth/register";
        }

        try {
            authService.register(RegisterRequest.builder()
                    .fullName(fullName.trim())
                    .email(email.trim())
                    .password(password)
                    .build());
            redirectAttributes.addFlashAttribute("success", "Account created successfully! Please sign in.");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }
}

