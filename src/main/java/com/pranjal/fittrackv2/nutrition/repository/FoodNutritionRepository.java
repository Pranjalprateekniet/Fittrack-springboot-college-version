package com.pranjal.fittrackv2.nutrition.repository;

import com.pranjal.fittrackv2.nutrition.entity.FoodNutrition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FoodNutritionRepository extends JpaRepository<FoodNutrition, Long> {

    Optional<FoodNutrition> findFirstByFoodNameIgnoreCase(String foodName);

    List<FoodNutrition> findTop10ByFoodNameStartingWithIgnoreCase(String query);

    List<FoodNutrition> findTop10ByFoodNameContainingIgnoreCase(String query);

    @Query(value = """
            SELECT *
            FROM food_nutrition f
            WHERE LOWER(f.food_name) = LOWER(:query)
               OR LOWER(f.food_name) LIKE LOWER(CONCAT(:query, ' %'))
               OR LOWER(f.food_name) LIKE LOWER(CONCAT('% ', :query, ' %'))
               OR LOWER(f.food_name) LIKE LOWER(CONCAT('% ', :query))
            """, nativeQuery = true)
    List<FoodNutrition> searchByWord(@Param("query") String query);
}
