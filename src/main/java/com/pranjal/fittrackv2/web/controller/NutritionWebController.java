package com.pranjal.fittrackv2.web.controller;

import com.pranjal.fittrackv2.nutrition.dto.FoodSearchResponse;
import com.pranjal.fittrackv2.nutrition.entity.MealType;
import com.pranjal.fittrackv2.nutrition.service.FoodNutritionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/nutrition")
@RequiredArgsConstructor
public class NutritionWebController {

    private final FoodNutritionService foodNutritionService;

    @GetMapping
    public String nutritionPage(@RequestParam(required = false) String query, Model model) {
        model.addAttribute("activePage", "nutrition");
        model.addAttribute("query", query != null ? query : "");
        model.addAttribute("mealTypes", MealType.values());
        model.addAttribute("today", LocalDate.now());

        if (query != null && !query.isBlank()) {
            try {
                List<FoodSearchResponse> results = foodNutritionService.searchFoodsAsDto(query);
                model.addAttribute("results", results);
                model.addAttribute("searched", true);
            } catch (Exception ex) {
                model.addAttribute("results", Collections.emptyList());
                model.addAttribute("error", "Food search failed: " + ex.getMessage());
                model.addAttribute("searched", true);
            }
        } else {
            model.addAttribute("results", Collections.emptyList());
            model.addAttribute("searched", false);
        }

        return "nutrition";
    }
}

