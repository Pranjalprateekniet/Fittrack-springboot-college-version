package com.pranjal.fittrackv2.nutrition.repository;

import com.pranjal.fittrackv2.entity.User;
import com.pranjal.fittrackv2.nutrition.entity.FoodLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface FoodLogRepository extends JpaRepository<FoodLog, Long> {
    List<FoodLog> findByUserAndLogDate(User user, LocalDate localDate);
}

