package com.pranjal.fittrackv2.dto;

import com.pranjal.fittrackv2.enums.WorkoutCategory;
import com.pranjal.fittrackv2.enums.WorkoutExercise;
import com.pranjal.fittrackv2.enums.WorkoutIntensity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateWorkoutRequest {
    @NotNull
    private WorkoutCategory category;

    @NotNull
    private WorkoutExercise exercise;

    private Integer sets;
    
    private Integer reps;

    @NotNull
    private WorkoutIntensity intensity;

    // Optional because Strength training does not require it
    @Positive
    private Integer durationMinutes;

    private LocalDate workoutDate;
}
