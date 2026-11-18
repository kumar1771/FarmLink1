package com.example.Order.Service.Service;

import com.example.Order.Service.Dto.OrderRequest;
import com.example.Order.Service.Dto.ProductResponse;
import com.example.Order.Service.Event.OrderPlacedEvent;
import com.example.Order.Service.Event.OrderStatusUpdatedEvent;
import com.example.Order.Service.Exception.InsufficientStockException;
import com.example.Order.Service.Exception.InvalidStatusTransitionException;
import com.example.Order.Service.Exception.ResourceNotFoundException;
import com.example.Order.Service.Exception.UnauthorizedException;
import com.example.Order.Service.Feign.ProductClient;
import com.example.Order.Service.Kafka.OrderKafkaProducer;
import com.example.Order.Service.Model.Order;
import com.example.Order.Service.Repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final OrderKafkaProducer kafkaProducer;

    @Transactional
    //name = "farmer-goods-service", fallbackMethod = "placeOrderFallback"
    @CircuitBreaker(name = "farmer-goods-service", fallbackMethod = "placeOrderFallback")
    public Order placeOrder(String buyerId, OrderRequest request) {
        ProductResponse product = productClient.getProduct(request.getProductId());

        if (request.getQuantity() > product.getQuantityAvailable()) {
            throw new InsufficientStockException("Not enough stock");
        }

        double totalPrice = product.getFarmerPrice() * request.getQuantity();
        productClient.reduceStock(request.getProductId(), request.getQuantity());

        Order order = Order.builder()
                .buyerId(buyerId)
                .farmerId(product.getFarmerId())
                .productId(product.getId())
                .productName(product.getProductName())
                .quantity(request.getQuantity())
                .pricePerUnit(product.getFarmerPrice())
                .totalPrice(totalPrice)
                .orderStatus("PLACED")
                .deliveryAddress(request.getDeliveryAddress())
                .orderDate(LocalDateTime.now())
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("Order placed: id={}, buyerId={}, productId={}", savedOrder.getId(), buyerId, request.getProductId());

        kafkaProducer.publishOrderPlaced(new OrderPlacedEvent(
                savedOrder.getId(), savedOrder.getBuyerId(), savedOrder.getFarmerId(),
                savedOrder.getProductName(), savedOrder.getQuantity(),
                savedOrder.getTotalPrice(), savedOrder.getDeliveryAddress()
        ));

        return savedOrder;
    }

    public Order placeOrderFallback(String buyerId, OrderRequest request, Throwable t) {
        log.error("Circuit breaker triggered for placeOrder. buyerId={}, cause={}", buyerId, t.getMessage());
        throw new ResourceNotFoundException("Product service is currently unavailable. Please try again later.");
    }

    public Page<Order> getBuyerOrders(String buyerId, Pageable pageable) {
        return orderRepository.findByBuyerId(buyerId, pageable);
    }

    public Page<Order> getFarmerOrders(String farmerId, Pageable pageable) {
        return orderRepository.findByFarmerId(farmerId, pageable);
    }

    @Transactional
    public Order updateOrderStatus(Long id, String userId, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        boolean isBuyer  = order.getBuyerId().equals(userId);
        boolean isFarmer = order.getFarmerId().equals(userId);

        if (!isBuyer && !isFarmer) {
            throw new UnauthorizedException("Unauthorized: you do not own this order");
        }

        String current = order.getOrderStatus();
        String next    = status.toUpperCase();

        validateTransition(current, next, isBuyer, isFarmer);

        order.setOrderStatus(next);
        Order savedOrder = orderRepository.save(order);
        log.info("Order status updated: id={}, from={}, to={}", id, current, next);

        kafkaProducer.publishOrderStatusUpdated(new OrderStatusUpdatedEvent(
                savedOrder.getId(), savedOrder.getBuyerId(), savedOrder.getProductName(), next
        ));

        return savedOrder;
    }

    private void validateTransition(String current, String next, boolean isBuyer, boolean isFarmer) {
        if ((next.equals("CONFIRMED") || next.equals("SHIPPED")) && !isFarmer) {
            throw new InvalidStatusTransitionException("Only the farmer can set status to " + next);
        }
        if (next.equals("DELIVERED") && !isBuyer) {
            throw new InvalidStatusTransitionException("Only the buyer can mark order as DELIVERED");
        }
        boolean valid = switch (current) {
            case "PLACED"    -> next.equals("CONFIRMED") || next.equals("CANCELLED");
            case "CONFIRMED" -> next.equals("SHIPPED")   || next.equals("CANCELLED");
            case "SHIPPED"   -> next.equals("DELIVERED");
            default          -> false;
        };
        if (!valid) {
            throw new InvalidStatusTransitionException("Invalid transition from " + current + " to " + next);
        }
    }
}
