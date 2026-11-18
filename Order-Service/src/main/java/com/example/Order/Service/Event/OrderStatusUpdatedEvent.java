package com.example.Order.Service.Event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdatedEvent {
    private Long orderId;
    private String buyerId;
    private String productName;
    private String newStatus;
}
