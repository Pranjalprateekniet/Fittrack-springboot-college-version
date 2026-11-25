package com.pranjal.fittrackv2.nutrition.controller;

import com.pranjal.fittrackv2.nutrition.client.USDAClient;
import com.pranjal.fittrackv2.nutrition.dto.FoodSearchResponse;
import com.pranjal.fittrackv2.nutrition.service.FoodNutritionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/nutrition")
public class FoodNutritionController {

    private final FoodNutritionService foodNutritionService;
    private final USDAClient usdaClient;

    @GetMapping("/search")
    public ResponseEntity<List<FoodSearchResponse>> searchFoods(@RequestParam String query) {
        return ResponseEntity.ok(foodNutritionService.searchFoodsAsDto(query));
    }

    @GetMapping("/usda")
    public ResponseEntity<List<FoodSearchResponse>> searchUSDA(@RequestParam String query) {
        return ResponseEntity.ok(usdaClient.searchFood(query));
    }
}

