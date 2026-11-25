package com.pranjal.fittrackv2.nutrition.dto;

import com.pranjal.fittrackv2.nutrition.entity.MealType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class FoodLogResponse {
    private String foodName;
    private Long foodId;
    private Double gramsConsumed;
    private MealType mealType;
    private LocalDate logDate;
    private Long logId;
}

