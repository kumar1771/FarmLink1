package com.example.Farmer.Goods.Service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_farmer_id", columnList = "farmerId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String farmerId;

    private String productName;

    private String category;

    private String type;

    private Double farmerPrice;

    private Double marketPrice;

    private Integer quantityAvailable;

    private String unit;

    private String location;

    private LocalDate harvestDate;

    private Boolean available;
}
