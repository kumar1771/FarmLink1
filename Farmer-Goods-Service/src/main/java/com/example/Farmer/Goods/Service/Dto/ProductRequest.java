package com.example.Farmer.Goods.Service.Dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    private String productName;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Type is required")
    private String type;

    @NotNull(message = "Farmer price is required")
    @Positive(message = "Farmer price must be positive")
    private Double farmerPrice;

    @NotNull(message = "Market price is required")
    @Positive(message = "Market price must be positive")
    private Double marketPrice;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantityAvailable;

    @NotBlank(message = "Unit is required")
    private String unit;

    @NotBlank(message = "Location is required")
    private String location;

    private LocalDate harvestDate;
}
