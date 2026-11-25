package com.pranjal.fittrackv2.goals.dto;

import com.pranjal.fittrackv2.goals.enums.GoalPace;
import com.pranjal.fittrackv2.goals.enums.GoalType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GoalResponse {
    private GoalType goalType;
    private GoalPace goalPace;

    private Integer targetCalories;
    private Double targetProtein;
    private Double targetCarbohydrates;
    private Double targetFat;

    private Double bmi;
    private String bmiCategory;
    private Double bmr;
    private Double tdee;

    private Double currentWeightKg;
    private Double targetWeightKg;
    private Integer estimatedWeeksToGoal;
}
