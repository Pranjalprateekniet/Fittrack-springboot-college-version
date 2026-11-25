package com.pranjal.fittrackv2.nutrition.dto;

import com.pranjal.fittrackv2.nutrition.entity.MealType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateFoodLogRequest {

    @NotNull
    private Long foodId;

    @NotNull
    @Positive(message = "Grams consumed must be greater than zero")
    private Double gramsConsumed;

    @NotNull
    private MealType mealType;

    private LocalDate logDate;
}

