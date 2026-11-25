package com.pranjal.fittrackv2.entity;

import com.pranjal.fittrackv2.enums.WorkoutCategory;
import com.pranjal.fittrackv2.enums.WorkoutExercise;
import com.pranjal.fittrackv2.enums.WorkoutIntensity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name="workouts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workout {
    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkoutCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkoutExercise exercise;

    @Column
    private Integer sets;

    @Column
    private Integer reps;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkoutIntensity intensity;

    @Column
    private Integer durationMinutes;

    @Column
    private Integer estimatedDuration;

    @Column
    private Double caloriesBurned;

    @Column
    private LocalDate workoutDate;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;
}
