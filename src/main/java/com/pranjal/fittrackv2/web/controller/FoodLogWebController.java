package com.pranjal.fittrackv2.web.controller;

import com.pranjal.fittrackv2.nutrition.dto.CreateFoodLogRequest;
import com.pranjal.fittrackv2.nutrition.dto.DailyNutritionSummaryResponse;
import com.pranjal.fittrackv2.nutrition.dto.FoodLogResponse;
import com.pranjal.fittrackv2.nutrition.dto.FoodSearchResponse;
import com.pranjal.fittrackv2.nutrition.entity.MealType;
import com.pranjal.fittrackv2.nutrition.service.FoodLogService;
import com.pranjal.fittrackv2.nutrition.service.FoodNutritionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/food-log")
@RequiredArgsConstructor
public class FoodLogWebController {

    private final FoodLogService foodLogService;
    private final FoodNutritionService foodNutritionService;

    @GetMapping
    public String foodLogPage(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) String query,
            Model model) {

        LocalDate selectedDate = date != null ? date : LocalDate.now();
        List<FoodLogResponse> logs = foodLogService.getLogsByDateResponse(selectedDate);
        DailyNutritionSummaryResponse summary = foodLogService.getSummaryByDate(selectedDate);

        List<FoodSearchResponse> searchResults = Collections.emptyList();
        if (query != null && !query.isBlank()) {
            try {
                searchResults = foodNutritionService.searchFoodsAsDto(query);
            } catch (Exception ex) {
                model.addAttribute("error", "Search error: " + ex.getMessage());
            }
        }

        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("logs", logs);
        model.addAttribute("summary", summary);
        model.addAttribute("mealTypes", MealType.values());
        model.addAttribute("query", query != null ? query : "");
        model.addAttribute("searchResults", searchResults);
        model.addAttribute("activePage", "food-log");
        return "food-log";
    }

    @PostMapping("/add")
    public String addFoodLog(
            @RequestParam Long foodId,
            @RequestParam Double gramsConsumed,
            @RequestParam MealType mealType,
            @RequestParam(required = false) LocalDate logDate,
            RedirectAttributes redirectAttributes) {
        LocalDate targetDate = logDate != null ? logDate : LocalDate.now();
        try {
            CreateFoodLogRequest req = new CreateFoodLogRequest();
            req.setFoodId(foodId);
            req.setGramsConsumed(gramsConsumed);
            req.setMealType(mealType);
            req.setLogDate(targetDate);
            foodLogService.createFoodLog(req);
            redirectAttributes.addFlashAttribute("success", "Food added to daily log.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/food-log?date=" + targetDate;
    }

    @PostMapping("/{logId}/update")
    public String updateFoodLog(
            @PathVariable Long logId,
            @RequestParam Long foodId,
            @RequestParam Double gramsConsumed,
            @RequestParam MealType mealType,
            @RequestParam(required = false) LocalDate logDate,
            RedirectAttributes redirectAttributes) {
        LocalDate targetDate = logDate != null ? logDate : LocalDate.now();
        try {
            CreateFoodLogRequest req = new CreateFoodLogRequest();
            req.setFoodId(foodId);
            req.setGramsConsumed(gramsConsumed);
            req.setMealType(mealType);
            req.setLogDate(targetDate);
            foodLogService.updateFoodLog(logId, req);
            redirectAttributes.addFlashAttribute("success", "Food log updated.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/food-log?date=" + targetDate;
    }

    @PostMapping("/{logId}/delete")
    public String deleteFoodLog(
            @PathVariable Long logId,
            @RequestParam(required = false) LocalDate date,
            RedirectAttributes redirectAttributes) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        try {
            foodLogService.deleteFoodLog(logId);
            redirectAttributes.addFlashAttribute("success", "Food log removed.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/food-log?date=" + targetDate;
    }
}

