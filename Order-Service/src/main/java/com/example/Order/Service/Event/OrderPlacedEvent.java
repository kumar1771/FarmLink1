package com.example.Order.Service.Event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlacedEvent {
    private Long orderId;
    private String buyerId;
    private String farmerId;
    private String productName;
    private Integer quantity;
    private Double totalPrice;
    private String deliveryAddress;
}
