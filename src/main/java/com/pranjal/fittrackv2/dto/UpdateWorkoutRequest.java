package com.pranjal.fittrackv2.dto;

import com.pranjal.fittrackv2.enums.WorkoutCategory;
import com.pranjal.fittrackv2.enums.WorkoutExercise;
import com.pranjal.fittrackv2.enums.WorkoutIntensity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateWorkoutRequest {
    @NotNull
    private WorkoutCategory category;

    @NotNull
    private WorkoutExercise exercise;

    private Integer sets;
    
    private Integer reps;

    @NotNull
    private WorkoutIntensity intensity;

    @Positive
    private Integer durationMinutes;

    @NotNull
    private LocalDate workoutDate;
}
