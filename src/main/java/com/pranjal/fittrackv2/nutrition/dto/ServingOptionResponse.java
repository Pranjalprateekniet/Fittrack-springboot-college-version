package com.pranjal.fittrackv2.nutrition.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServingOptionResponse {
    private UUID id;
    private String displayName;
    private Double servingWeightGrams;
    private Boolean isDefault;
}

