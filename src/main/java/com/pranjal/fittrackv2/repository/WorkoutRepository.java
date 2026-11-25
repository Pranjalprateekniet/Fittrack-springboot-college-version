package com.pranjal.fittrackv2.repository;

import com.pranjal.fittrackv2.entity.User;
import com.pranjal.fittrackv2.entity.Workout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkoutRepository extends JpaRepository<Workout, UUID> {
    List<Workout> findByUser(User user);
    Optional<Workout> findById(UUID id);


}

