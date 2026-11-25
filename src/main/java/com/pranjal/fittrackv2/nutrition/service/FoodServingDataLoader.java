package com.pranjal.fittrackv2.nutrition.service;

import com.pranjal.fittrackv2.nutrition.entity.FoodNutrition;
import com.pranjal.fittrackv2.nutrition.entity.FoodServing;
import com.pranjal.fittrackv2.nutrition.repository.FoodNutritionRepository;
import com.pranjal.fittrackv2.nutrition.repository.FoodServingRepository;
import com.opencsv.CSVReader;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Order(2)
@Slf4j
public class FoodServingDataLoader {

    private final FoodNutritionRepository foodNutritionRepository;
    private final FoodServingRepository foodServingRepository;

    @PostConstruct
    public void loadServings() {
        try {
            ClassPathResource resource = new ClassPathResource("data/food_servings.csv");
            try (CSVReader csvReader = new CSVReader(new InputStreamReader(resource.getInputStream()))) {
                List<String[]> rows = csvReader.readAll();
                if (!rows.isEmpty()) {
                    rows.remove(0);
                }

                int loaded = 0;
                int skipped = 0;

                for (String[] row : rows) {
                    if (row.length < 5) {
                        skipped++;
                        continue;
                    }

                    String foodName = row[0].trim();
                    String displayName = row[1].trim();
                    double servingWeightGrams = parseDouble(row[2], 100.0);
                    int displayOrder = parseInteger(row[3], 1);
                    boolean isDefault = Boolean.parseBoolean(row[4].trim());

                    Optional<FoodNutrition> foodOpt = foodNutritionRepository.findFirstByFoodNameIgnoreCase(foodName);
                    if (foodOpt.isEmpty()) {
                        skipped++;
                        continue;
                    }

                    FoodNutrition food = foodOpt.get();
                    boolean alreadyExists = foodServingRepository
                            .findByFoodIdOrderByDisplayOrderAsc(food.getId())
                            .stream()
                            .anyMatch(s -> s.getDisplayName().equalsIgnoreCase(displayName));

                    if (alreadyExists) {
                        skipped++;
                        continue;
                    }

                    FoodServing serving = FoodServing.builder()
                            .food(food)
                            .displayName(displayName)
                            .servingWeightGrams(servingWeightGrams)
                            .displayOrder(displayOrder)
                            .isDefault(isDefault)
                            .build();

                    foodServingRepository.save(serving);
                    loaded++;
                }

                log.info("FoodServingDataLoader done — {} servings loaded, {} skipped", loaded, skipped);
            }
        } catch (Exception e) {
            log.error("Failed to load food_servings.csv", e);
        }
    }

    private double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private int parseInteger(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
