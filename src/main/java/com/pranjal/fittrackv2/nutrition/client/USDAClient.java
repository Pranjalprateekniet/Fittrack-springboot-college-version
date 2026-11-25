package com.pranjal.fittrackv2.nutrition.client;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.pranjal.fittrackv2.nutrition.dto.FoodSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class USDAClient {
    private final WebClient.Builder webClientBuilder;

    @Value("${usda.api.key:}")
    private String apiKey;

    public List<FoodSearchResponse> searchFood(String query) {
        if (apiKey == null || apiKey.isBlank()) {
            return Collections.emptyList();
        }
        try {
            List<FoodSearchResponse> foods = searchFoodFromUsda(query, true);
            if (foods.isEmpty()) {
                foods = searchFoodFromUsda(query, false);
            }
            return foods;
        } catch (Exception e) {
            log.warn("USDA lookup failed for '{}': {}", query, e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<FoodSearchResponse> searchFoodFromUsda(String query, boolean rawOnly) {
        String response = webClientBuilder.build()
                .get()
                .uri(uriBuilder -> {
                    uriBuilder
                            .scheme("https")
                            .host("api.nal.usda.gov")
                            .path("/fdc/v1/foods/search")
                            .queryParam("query", query)
                            .queryParam("pageSize", 10)
                            .queryParam("api_key", apiKey);
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (response == null || response.isBlank()) {
            return Collections.emptyList();
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            JsonNode foods = root.get("foods");
            if (foods == null || !foods.isArray()) {
                return Collections.emptyList();
            }

            List<FoodSearchResponse> result = new ArrayList<>();
            for (JsonNode food : foods) {
                String foodName = food.path("description").asText("");
                String dataType = food.path("dataType").asText("USDA");

                Double calories = 0.0;
                Double protein = 0.0;
                Double carbs = 0.0;
                Double fat = 0.0;
                Double fiber = 0.0;
                Double cholesterol = 0.0;
                Double freeSugar = 0.0;
                Double servingSize = 100.0;

                if (food.has("servingSize")) {
                    servingSize = food.path("servingSize").asDouble(100.0);
                    if (servingSize <= 0) {
                        servingSize = 100.0;
                    }
                }

                JsonNode nutrients = food.get("foodNutrients");
                if (nutrients != null) {
                    for (JsonNode nutrient : nutrients) {
                        String nutrientName = nutrient.path("nutrientName").asText();
                        Double value = nutrient.path("value").asDouble(0.0);

                        if (nutrientName.equalsIgnoreCase("Energy")) {
                            calories = value;
                        } else if (nutrientName.equalsIgnoreCase("Protein")) {
                            protein = value;
                        } else if (nutrientName.equalsIgnoreCase("Carbohydrate, by difference")) {
                            carbs = value;
                        } else if (nutrientName.equalsIgnoreCase("Total lipid (fat)")) {
                            fat = value;
                        } else if (nutrientName.equalsIgnoreCase("Fiber, total dietary")) {
                            fiber = value;
                        } else if (nutrientName.equalsIgnoreCase("Cholesterol")) {
                            cholesterol = value;
                        } else if (nutrientName.equalsIgnoreCase("Sugars, added")) {
                            freeSugar = value;
                        } else if (nutrientName.equalsIgnoreCase("Total Sugars") && freeSugar == 0.0) {
                            freeSugar = value;
                        }
                    }
                }

                result.add(new FoodSearchResponse(
                        foodName, calories, protein, carbs, fat, fiber,
                        cholesterol, freeSugar, servingSize, dataType
                ));
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse USDA response", e);
        }
    }
}
