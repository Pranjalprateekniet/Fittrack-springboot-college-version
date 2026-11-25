package com.pranjal.fittrackv2.web.controller;

import com.pranjal.fittrackv2.entity.User;
import com.pranjal.fittrackv2.goals.Service.GoalsService;
import com.pranjal.fittrackv2.goals.dto.GoalResponse;
import com.pranjal.fittrackv2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileWebController {

    private final UserRepository userRepository;
    private final GoalsService goalsService;

    @GetMapping
    public String profilePage(Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        model.addAttribute("user", user);
        model.addAttribute("activePage", "profile");

        GoalResponse goal = null;
        try {
            goal = goalsService.getGoal(email);
        } catch (RuntimeException ignored) {
        }
        model.addAttribute("goal", goal);

        return "profile";
    }
}

