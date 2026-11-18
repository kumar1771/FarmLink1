package com.example.Order.Service.Controller;

import com.example.Order.Service.Dto.OrderRequest;
import com.example.Order.Service.Model.Order;
import com.example.Order.Service.Service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> placeOrder(
            @RequestHeader("X-User-Id") String buyerId,
            @Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.placeOrder(buyerId, request));
    }

    @GetMapping("/buyer")
    public Page<Order> getBuyerOrders(
            @RequestHeader("X-User-Id") String buyerId,
            @PageableDefault(size = 20, sort = "orderDate") Pageable pageable) {
        return orderService.getBuyerOrders(buyerId, pageable);
    }

    @GetMapping("/farmer")
    public Page<Order> getFarmerOrders(
            @RequestHeader("X-User-Id") String farmerId,
            @PageableDefault(size = 20, sort = "orderDate") Pageable pageable) {
        return orderService.getFarmerOrders(farmerId, pageable);
    }

    @PutMapping("/{id}/status")
    public Order updateStatus(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long id,
            @RequestParam String status) {
        return orderService.updateOrderStatus(id, userId, status);
    }
}
