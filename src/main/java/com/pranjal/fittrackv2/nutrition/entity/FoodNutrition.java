package com.pranjal.fittrackv2.nutrition.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "food_nutrition")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FoodNutrition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String foodName;

    private String foodType;

    private Integer calories;

    private Double carbsG;

    private Double proteinG;

    private Double fatG;

    private Double freeSugarG;

    private Double fiberG;

    private Double cholesterolMg;

    private Double servingSizeG;

    private Double proteinCalorieRatio;

    private String source;

    @OneToMany(mappedBy = "food", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<FoodServing> servings = new ArrayList<>();
}

