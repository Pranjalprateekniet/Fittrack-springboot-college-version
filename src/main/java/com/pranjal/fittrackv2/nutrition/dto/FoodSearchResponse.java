package com.pranjal.fittrackv2.nutrition.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodSearchResponse {

    private Long id;
    private String foodName;
    private Double calories;
    private Double protein;
    private Double carbs;
    private Double fat;
    private Double fiber;
    private Double cholesterol;
    private Double freeSugar;
    private Double servingSizeG;
    private String dataType;

    private List<ServingOptionResponse> servings = new ArrayList<>();

    public FoodSearchResponse(String foodName, Double calories, Double protein,
                              Double carbs, Double fat, Double fiber,
                              Double cholesterol, Double freeSugar,
                              Double servingSizeG, String dataType) {
        this.id = null;
        this.foodName = foodName;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.fiber = fiber;
        this.cholesterol = cholesterol;
        this.freeSugar = freeSugar;
        this.servingSizeG = servingSizeG;
        this.dataType = dataType;
        this.servings = new ArrayList<>();
    }
}

