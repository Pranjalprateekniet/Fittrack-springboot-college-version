package com.pranjal.fittrackv2.nutrition.service;

import com.pranjal.fittrackv2.nutrition.entity.FoodNutrition;
import com.pranjal.fittrackv2.nutrition.repository.FoodNutritionRepository;
import com.opencsv.CSVReader;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
@Order(1)
@Slf4j
public class FoodNutritionDataLoader {

    private final FoodNutritionRepository foodNutritionRepository;

    @PostConstruct
    public void loadData() {
        if (foodNutritionRepository.count() > 0) {
            return;
        }

        try {
            ClassPathResource resource = new ClassPathResource("data/food_nutrition_cleaned.csv");
            try (CSVReader csvReader = new CSVReader(new InputStreamReader(resource.getInputStream()))) {
                List<String[]> rows = csvReader.readAll();
                if (!rows.isEmpty()) {
                    rows.remove(0);
                }

                List<FoodNutrition> foods = new ArrayList<>();
                for (String[] row : rows) {
                    if (row.length < 11) continue;
                    FoodNutrition food = FoodNutrition.builder()
                            .foodName(row[0])
                            .foodType(row[1])
                            .calories(parseInteger(row[2]))
                            .carbsG(parseDouble(row[3]))
                            .proteinG(parseDouble(row[4]))
                            .fatG(parseDouble(row[5]))
                            .freeSugarG(parseDouble(row[6]))
                            .fiberG(parseDouble(row[7]))
                            .cholesterolMg(parseDouble(row[8]))
                            .servingSizeG(parseDouble(row[9]))
                            .proteinCalorieRatio(parseDouble(row[10]))
                            .source("LOCAL")
                            .build();

                    if (isInvalidFood(food)) {
                        continue;
                    }
                    foods.add(food);
                }

                foodNutritionRepository.saveAll(foods);
                log.info("Food nutrition data loaded successfully ({} records)", foods.size());
            }
        } catch (Exception e) {
            log.error("Failed to load food_nutrition_cleaned.csv", e);
        }
    }

    private boolean isInvalidFood(FoodNutrition food) {
        return (food.getFoodName() == null || food.getFoodName().isBlank()) ||
                (food.getCalories() != null && food.getCalories() < 0) ||
                (food.getCarbsG() != null && food.getCarbsG() < 0) ||
                (food.getProteinG() != null && food.getProteinG() < 0) ||
                (food.getFatG() != null && food.getFatG() < 0) ||
                (food.getFreeSugarG() != null && food.getFreeSugarG() < 0) ||
                (food.getFiberG() != null && food.getFiberG() < 0) ||
                (food.getCholesterolMg() != null && food.getCholesterolMg() < 0) ||
                (food.getServingSizeG() != null && food.getServingSizeG() <= 0);
    }

    private Double parseDouble(String value) {
        if (value == null || value.isBlank()) return null;
        return Double.parseDouble(value);
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) return null;
        return Integer.parseInt(value);
    }
}

