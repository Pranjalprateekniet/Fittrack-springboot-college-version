package com.pranjal.fittrackv2.nutrition.repository;

import com.pranjal.fittrackv2.nutrition.entity.FoodServing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FoodServingRepository extends JpaRepository<FoodServing, UUID> {

    List<FoodServing> findByFoodIdOrderByDisplayOrderAsc(Long foodId);

    Optional<FoodServing> findByFoodIdAndIsDefaultTrue(Long foodId);
}

