package com.pranjal.fittrackv2.dto;

import com.pranjal.fittrackv2.enums.WorkoutCategory;
import com.pranjal.fittrackv2.enums.WorkoutExercise;
import com.pranjal.fittrackv2.enums.WorkoutIntensity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class WorkoutResponse {
    private UUID id;
    private WorkoutCategory category;
    private WorkoutExercise exercise;
    private Integer sets;
    private Integer reps;
    private WorkoutIntensity intensity;
    private Integer durationMinutes;
    private Integer estimatedDuration;
    private Double caloriesBurned;
    private LocalDate workoutDate;
}
