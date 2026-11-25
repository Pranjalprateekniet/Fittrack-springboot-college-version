package com.pranjal.fittrackv2.goals.repository;

import com.pranjal.fittrackv2.entity.User;
import com.pranjal.fittrackv2.goals.entity.GoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GoalRepository extends JpaRepository<GoalEntity, UUID> {
    Optional<GoalEntity> findByUser(User user);
}
