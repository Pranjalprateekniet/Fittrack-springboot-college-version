package com.pranjal.fittrackv2.nutrition.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "food_serving")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodServing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "food_nutrition_id", nullable = false)
    @JsonBackReference
    private FoodNutrition food;

    @Column(nullable = false)
    private String displayName;

    @Column(nullable = false)
    private Double servingWeightGrams;

    @Column(nullable = false)
    private Integer displayOrder;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isDefault = false;
}

