package com.pranjal.fittrackv2.web.controller;

import com.pranjal.fittrackv2.dto.CreateWorkoutRequest;
import com.pranjal.fittrackv2.dto.UpdateWorkoutRequest;
import com.pranjal.fittrackv2.dto.WorkoutResponse;
import com.pranjal.fittrackv2.enums.WorkoutCategory;
import com.pranjal.fittrackv2.enums.WorkoutExercise;
import com.pranjal.fittrackv2.enums.WorkoutIntensity;
import com.pranjal.fittrackv2.service.WorkoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/workouts")
@RequiredArgsConstructor
public class WorkoutWebController {

    private final WorkoutService workoutService;

    @GetMapping
    public String workoutsPage(Model model) {
        List<WorkoutResponse> workouts = workoutService.getMyWorkouts().stream()
                .sorted(Comparator.comparing(
                        WorkoutResponse::getWorkoutDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        double totalCalories = workouts.stream()
                .mapToDouble(w -> w.getCaloriesBurned() != null ? w.getCaloriesBurned() : 0.0)
                .sum();
        int totalMinutes = workouts.stream()
                .mapToInt(w -> w.getEstimatedDuration() != null ? w.getEstimatedDuration() : 0)
                .sum();

        model.addAttribute("workouts", workouts);
        model.addAttribute("totalCalories", Math.round(totalCalories));
        model.addAttribute("totalMinutes", totalMinutes);
        model.addAttribute("categories", WorkoutCategory.values());
        model.addAttribute("exercises", WorkoutExercise.values());
        model.addAttribute("intensities", WorkoutIntensity.values());
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("activePage", "workouts");
        return "workouts";
    }

    @PostMapping
    public String createWorkout(
            @RequestParam WorkoutCategory category,
            @RequestParam WorkoutExercise exercise,
            @RequestParam WorkoutIntensity intensity,
            @RequestParam(required = false) Integer sets,
            @RequestParam(required = false) Integer reps,
            @RequestParam(required = false) Integer durationMinutes,
            @RequestParam(required = false) LocalDate workoutDate,
            RedirectAttributes redirectAttributes) {
        try {
            CreateWorkoutRequest req = new CreateWorkoutRequest();
            req.setCategory(category);
            req.setExercise(exercise);
            req.setIntensity(intensity);
            req.setSets(sets);
            req.setReps(reps);
            req.setDurationMinutes(durationMinutes);
            req.setWorkoutDate(workoutDate != null ? workoutDate : LocalDate.now());
            workoutService.createWorkout(req);
            redirectAttributes.addFlashAttribute("success", "Workout logged successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/workouts";
    }

    @PostMapping("/{id}/update")
    public String updateWorkout(
            @PathVariable UUID id,
            @RequestParam WorkoutCategory category,
            @RequestParam WorkoutExercise exercise,
            @RequestParam WorkoutIntensity intensity,
            @RequestParam(required = false) Integer sets,
            @RequestParam(required = false) Integer reps,
            @RequestParam(required = false) Integer durationMinutes,
            @RequestParam(required = false) LocalDate workoutDate,
            RedirectAttributes redirectAttributes) {
        try {
            UpdateWorkoutRequest req = new UpdateWorkoutRequest();
            req.setCategory(category);
            req.setExercise(exercise);
            req.setIntensity(intensity);
            req.setSets(sets);
            req.setReps(reps);
            req.setDurationMinutes(durationMinutes);
            req.setWorkoutDate(workoutDate != null ? workoutDate : LocalDate.now());
            workoutService.updateWorkout(id, req);
            redirectAttributes.addFlashAttribute("success", "Workout updated successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/workouts";
    }

    @PostMapping("/{id}/delete")
    public String deleteWorkout(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            workoutService.deleteWorkout(id);
            redirectAttributes.addFlashAttribute("success", "Workout deleted.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/workouts";
    }
}

