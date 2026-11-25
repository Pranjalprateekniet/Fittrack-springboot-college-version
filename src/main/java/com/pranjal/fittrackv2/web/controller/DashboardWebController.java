package com.pranjal.fittrackv2.web.controller;

import com.pranjal.fittrackv2.dto.WorkoutResponse;
import com.pranjal.fittrackv2.entity.User;
import com.pranjal.fittrackv2.goals.Service.GoalsService;
import com.pranjal.fittrackv2.goals.dto.GoalResponse;
import com.pranjal.fittrackv2.nutrition.dto.DailyNutritionSummaryResponse;
import com.pranjal.fittrackv2.nutrition.dto.FoodLogResponse;
import com.pranjal.fittrackv2.nutrition.service.FoodLogService;
import com.pranjal.fittrackv2.repository.UserRepository;
import com.pranjal.fittrackv2.service.WorkoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardWebController {

    private final UserRepository userRepository;
    private final GoalsService goalsService;
    private final WorkoutService workoutService;
    private final FoodLogService foodLogService;

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        model.addAttribute("user", user);
        model.addAttribute("activePage", "dashboard");

        GoalResponse goal = null;
        try {
            goal = goalsService.getGoal(email);
        } catch (RuntimeException ignored) {
            // User has not configured a goal yet
        }
        model.addAttribute("goal", goal);

        List<WorkoutResponse> workouts = workoutService.getMyWorkouts();
        double totalWorkoutCalories = workouts.stream()
                .mapToDouble(w -> w.getCaloriesBurned() != null ? w.getCaloriesBurned() : 0.0)
                .sum();
        int totalWorkoutMinutes = workouts.stream()
                .mapToInt(w -> w.getEstimatedDuration() != null ? w.getEstimatedDuration() : 0)
                .sum();

        model.addAttribute("workoutCount", workouts.size());
        model.addAttribute("totalWorkoutCalories", Math.round(totalWorkoutCalories));
        model.addAttribute("totalWorkoutMinutes", totalWorkoutMinutes);
        model.addAttribute("recentWorkouts", workouts.stream().limit(5).toList());

        DailyNutritionSummaryResponse nutritionSummary = foodLogService.getTodaySummary();
        List<FoodLogResponse> todayFoodLogs = foodLogService.getTodayLogs();
        model.addAttribute("nutritionSummary", nutritionSummary);
        model.addAttribute("todayFoodCount", todayFoodLogs.size());

        return "dashboard";
    }
}

