package com.pranjal.fittrackv2.web.controller;

import com.pranjal.fittrackv2.goals.Service.GoalsService;
import com.pranjal.fittrackv2.goals.dto.CreateGoalRequest;
import com.pranjal.fittrackv2.goals.dto.GoalResponse;
import com.pranjal.fittrackv2.goals.enums.ActivityLevel;
import com.pranjal.fittrackv2.goals.enums.Gender;
import com.pranjal.fittrackv2.goals.enums.GoalPace;
import com.pranjal.fittrackv2.goals.enums.GoalType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/goals")
@RequiredArgsConstructor
public class GoalWebController {

    private final GoalsService goalsService;

    private void populateEnums(Model model) {
        model.addAttribute("goalTypes", GoalType.values());
        model.addAttribute("goalPaces", GoalPace.values());
        model.addAttribute("genders", Gender.values());
        model.addAttribute("activityLevels", ActivityLevel.values());
        model.addAttribute("activePage", "goals");
    }

    @GetMapping
    public String goalsPage(Authentication authentication, Model model) {
        populateEnums(model);
        String email = authentication.getName();

        GoalResponse goal = null;
        try {
            goal = goalsService.getGoal(email);
        } catch (RuntimeException ignored) {
        }
        model.addAttribute("goal", goal);

        if (!model.containsAttribute("goalRequest")) {
            CreateGoalRequest req = new CreateGoalRequest();
            if (goal != null) {
                req.setGoalType(goal.getGoalType());
                req.setGoalPace(goal.getGoalPace());
                req.setWeightKg(goal.getCurrentWeightKg());
                req.setTargetWeightKg(goal.getTargetWeightKg());
            } else {
                req.setGoalType(GoalType.WEIGHT_LOSS);
                req.setGoalPace(GoalPace.MODERATE);
                req.setGender(Gender.MALE);
                req.setActivityLevel(ActivityLevel.MODERATELY_ACTIVE);
                req.setWeightKg(75.0);
                req.setHeightCm(175.0);
                req.setTargetWeightKg(70.0);
                req.setAge(22);
            }
            model.addAttribute("goalRequest", req);
        }

        return "goals";
    }

    @PostMapping
    public String saveGoal(
            Authentication authentication,
            @Valid @ModelAttribute("goalRequest") CreateGoalRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            populateEnums(model);
            try {
                model.addAttribute("goal", goalsService.getGoal(authentication.getName()));
            } catch (RuntimeException ignored) {
            }
            model.addAttribute("error", "Please check the form fields and ensure all values are within valid ranges.");
            return "goals";
        }

        try {
            goalsService.createOrUpdateGoal(authentication.getName(), request);
            redirectAttributes.addFlashAttribute("success", "Fitness goal saved and metrics recalculated.");
            return "redirect:/goals";
        } catch (RuntimeException ex) {
            populateEnums(model);
            try {
                model.addAttribute("goal", goalsService.getGoal(authentication.getName()));
            } catch (RuntimeException ignored) {
            }
            model.addAttribute("error", ex.getMessage());
            return "goals";
        }
    }

    @PostMapping("/delete")
    public String deleteGoal(Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            goalsService.deleteGoal(authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Fitness goal deleted.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/goals";
    }
}

