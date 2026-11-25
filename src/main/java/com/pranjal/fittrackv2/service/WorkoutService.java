package com.pranjal.fittrackv2.service;


import com.pranjal.fittrackv2.dto.CreateWorkoutRequest;
import com.pranjal.fittrackv2.dto.UpdateWorkoutRequest;
import com.pranjal.fittrackv2.dto.WorkoutResponse;
import com.pranjal.fittrackv2.entity.User;
import com.pranjal.fittrackv2.entity.Workout;
import com.pranjal.fittrackv2.enums.WorkoutCategory;
import com.pranjal.fittrackv2.exception.UnauthorizedWorkoutAccessRequestException;
import com.pranjal.fittrackv2.exception.WorkoutNotFoundException;
import com.pranjal.fittrackv2.goals.entity.GoalEntity;
import com.pranjal.fittrackv2.goals.repository.GoalRepository;
import com.pranjal.fittrackv2.repository.UserRepository;
import com.pranjal.fittrackv2.repository.WorkoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkoutService {
    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;
    private final GoalRepository goalRepository;

    private final WorkoutCalorieService workoutCalorieService;

    public String createWorkout(CreateWorkoutRequest request){
        User user = getAuthenticatedUser();
        
        Integer duration = request.getDurationMinutes();
        if (request.getCategory() == WorkoutCategory.STRENGTH) {
            duration = workoutCalorieService.estimateStrengthDuration(request.getSets(), request.getReps(), request.getIntensity());
        }

        GoalEntity goal = goalRepository.findByUser(user).orElse(null);
        Double weightKg = (goal != null) ? goal.getWeightKg() : 70.0;

        double calories = workoutCalorieService.calculateCaloriesBurned(
                request.getCategory(), 
                request.getExercise(), 
                request.getIntensity(), 
                duration, 
                weightKg
        );

        Workout workout = Workout.builder()
                .category(request.getCategory())
                .exercise(request.getExercise())
                .sets(request.getSets())
                .reps(request.getReps())
                .intensity(request.getIntensity())
                .durationMinutes(request.getDurationMinutes())
                .estimatedDuration(duration)
                .caloriesBurned(calories)
                .workoutDate(request.getWorkoutDate())
                .user(user)
                .build();
        workoutRepository.save(workout);
        return "Workout created successfully";
    }

    public List<WorkoutResponse> getMyWorkouts(){
        User user = getAuthenticatedUser();
        List<Workout> workouts = workoutRepository.findByUser(user);
        List<WorkoutResponse> responses = new ArrayList<>();
        for(Workout workout : workouts){
            responses.add(WorkoutResponse.builder()
                .id(workout.getId())
                .category(workout.getCategory())
                .exercise(workout.getExercise())
                .sets(workout.getSets())
                .reps(workout.getReps())
                .intensity(workout.getIntensity())
                .durationMinutes(workout.getDurationMinutes())
                .estimatedDuration(workout.getEstimatedDuration())
                .caloriesBurned(workout.getCaloriesBurned())
                .workoutDate(workout.getWorkoutDate())
                .build());
        }
        return responses;
    }

    private User getAuthenticatedUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email).orElseThrow(()-> new UnauthorizedWorkoutAccessRequestException("User not found"));
    }

    public void deleteWorkout(UUID workoutId){
        User user = getAuthenticatedUser();
        Workout workout = workoutRepository.findById(workoutId).orElseThrow(()->new WorkoutNotFoundException("Workout not found"));
        if(!workout.getUser().getId().equals(user.getId())){
            throw new UnauthorizedWorkoutAccessRequestException("You are not authorized to delete this workout");
        }
        workoutRepository.delete(workout);
    }

    public WorkoutResponse updateWorkout(UUID workoutId, UpdateWorkoutRequest request){
        User user = getAuthenticatedUser();

        Workout workout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new WorkoutNotFoundException("Workout not found"));

        if (!workout.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedWorkoutAccessRequestException(
                    "You are not authorized to update this workout"
            );
        }

        Integer duration = request.getDurationMinutes();
        if (request.getCategory() == WorkoutCategory.STRENGTH) {
            duration = workoutCalorieService.estimateStrengthDuration(request.getSets(), request.getReps(), request.getIntensity());
        }

        GoalEntity goal = goalRepository.findByUser(user).orElse(null);
        Double weightKg = (goal != null) ? goal.getWeightKg() : 70.0;

        double calories = workoutCalorieService.calculateCaloriesBurned(
                request.getCategory(), 
                request.getExercise(), 
                request.getIntensity(), 
                duration, 
                weightKg
        );

        workout.setCategory(request.getCategory());
        workout.setExercise(request.getExercise());
        workout.setSets(request.getSets());
        workout.setReps(request.getReps());
        workout.setIntensity(request.getIntensity());
        workout.setDurationMinutes(request.getDurationMinutes());
        workout.setEstimatedDuration(duration);
        workout.setCaloriesBurned(calories);
        workout.setWorkoutDate(request.getWorkoutDate());

        Workout updatedWorkout = workoutRepository.save(workout);

        return WorkoutResponse.builder()
                .id(updatedWorkout.getId())
                .category(updatedWorkout.getCategory())
                .exercise(updatedWorkout.getExercise())
                .sets(updatedWorkout.getSets())
                .reps(updatedWorkout.getReps())
                .intensity(updatedWorkout.getIntensity())
                .durationMinutes(updatedWorkout.getDurationMinutes())
                .estimatedDuration(updatedWorkout.getEstimatedDuration())
                .caloriesBurned(updatedWorkout.getCaloriesBurned())
                .workoutDate(updatedWorkout.getWorkoutDate())
                .build();
    }
}
