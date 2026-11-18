package com.example.Order.Service.Service;

import com.example.Order.Service.Dto.OrderRequest;
import com.example.Order.Service.Dto.ProductResponse;
import com.example.Order.Service.Exception.InsufficientStockException;
import com.example.Order.Service.Exception.InvalidStatusTransitionException;
import com.example.Order.Service.Exception.ResourceNotFoundException;
import com.example.Order.Service.Exception.UnauthorizedException;
import com.example.Order.Service.Feign.ProductClient;
import com.example.Order.Service.Kafka.OrderKafkaProducer;
import com.example.Order.Service.Model.Order;
import com.example.Order.Service.Repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ProductClient productClient;
    @Mock private OrderKafkaProducer kafkaProducer;
    @InjectMocks private OrderService orderService;

    private OrderRequest orderRequest;
    private ProductResponse productResponse;
    private Order existingOrder;
    private final String buyerId  = "buyer@example.com";
    private final String farmerId = "farmer@example.com";
    private final Long orderId    = 1L;
    private final Long productId  = 10L;
    private final PageRequest pageable = PageRequest.of(0, 20);

    @BeforeEach
    void setUp() {
        orderRequest = new OrderRequest();
        orderRequest.setProductId(productId);
        orderRequest.setQuantity(5);
        orderRequest.setDeliveryAddress("123 London Road");

        productResponse = new ProductResponse();
        productResponse.setId(productId);
        productResponse.setFarmerId(farmerId);
        productResponse.setProductName("Tomatoes");
        productResponse.setFarmerPrice(2.50);
        productResponse.setQuantityAvailable(100);

        existingOrder = Order.builder()
                .id(orderId)
                .buyerId(buyerId)
                .farmerId(farmerId)
                .productId(productId)
                .productName("Tomatoes")
                .quantity(5)
                .pricePerUnit(2.50)
                .totalPrice(12.50)
                .orderStatus("PLACED")
                .deliveryAddress("123 London Road")
                .build();
    }

    // ─── placeOrder ───────────────────────────────────────────────────────────

    @Test
    void placeOrder_success() {
        when(productClient.getProduct(productId)).thenReturn(productResponse);
        doNothing().when(productClient).reduceStock(productId, 5);
        when(orderRepository.save(any(Order.class))).thenReturn(existingOrder);
        doNothing().when(kafkaProducer).publishOrderPlaced(any());

        Order result = orderService.placeOrder(buyerId, orderRequest);

        assertEquals(buyerId, result.getBuyerId());
        assertEquals(farmerId, result.getFarmerId());
        assertEquals("PLACED", result.getOrderStatus());
        assertEquals(12.50, result.getTotalPrice());
        verify(productClient, times(1)).reduceStock(productId, 5);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(kafkaProducer, times(1)).publishOrderPlaced(any());
    }

    @Test
    void placeOrder_insufficientStock_throwsException() {
        orderRequest.setQuantity(200);
        when(productClient.getProduct(productId)).thenReturn(productResponse);

        assertThrows(InsufficientStockException.class,
                () -> orderService.placeOrder(buyerId, orderRequest));
        verify(orderRepository, never()).save(any());
        verify(productClient, never()).reduceStock(any(), anyInt());
    }

    // ─── getBuyerOrders ───────────────────────────────────────────────────────

    @Test
    void getBuyerOrders_returnsList() {
        Page<Order> page = new PageImpl<>(List.of(existingOrder));
        when(orderRepository.findByBuyerId(buyerId, pageable)).thenReturn(page);

        Page<Order> result = orderService.getBuyerOrders(buyerId, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(buyerId, result.getContent().get(0).getBuyerId());
    }

    @Test
    void getBuyerOrders_noOrders_returnsEmptyPage() {
        when(orderRepository.findByBuyerId(buyerId, pageable)).thenReturn(Page.empty());

        assertTrue(orderService.getBuyerOrders(buyerId, pageable).isEmpty());
    }

    // ─── getFarmerOrders ──────────────────────────────────────────────────────

    @Test
    void getFarmerOrders_returnsList() {
        Page<Order> page = new PageImpl<>(List.of(existingOrder));
        when(orderRepository.findByFarmerId(farmerId, pageable)).thenReturn(page);

        Page<Order> result = orderService.getFarmerOrders(farmerId, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(farmerId, result.getContent().get(0).getFarmerId());
    }

    // ─── updateOrderStatus ────────────────────────────────────────────────────

    @Test
    void updateOrderStatus_farmerConfirms_success() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(existingOrder);
        doNothing().when(kafkaProducer).publishOrderStatusUpdated(any());

        Order result = orderService.updateOrderStatus(orderId, farmerId, "CONFIRMED");

        assertEquals("CONFIRMED", result.getOrderStatus());
        verify(kafkaProducer, times(1)).publishOrderStatusUpdated(any());
    }

    @Test
    void updateOrderStatus_farmerShips_success() {
        existingOrder.setOrderStatus("CONFIRMED");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(existingOrder);
        doNothing().when(kafkaProducer).publishOrderStatusUpdated(any());

        Order result = orderService.updateOrderStatus(orderId, farmerId, "SHIPPED");

        assertEquals("SHIPPED", result.getOrderStatus());
    }

    @Test
    void updateOrderStatus_buyerDelivers_success() {
        existingOrder.setOrderStatus("SHIPPED");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(existingOrder);
        doNothing().when(kafkaProducer).publishOrderStatusUpdated(any());

        Order result = orderService.updateOrderStatus(orderId, buyerId, "DELIVERED");

        assertEquals("DELIVERED", result.getOrderStatus());
    }

    @Test
    void updateOrderStatus_buyerCancels_success() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(existingOrder);
        doNothing().when(kafkaProducer).publishOrderStatusUpdated(any());

        Order result = orderService.updateOrderStatus(orderId, buyerId, "CANCELLED");

        assertEquals("CANCELLED", result.getOrderStatus());
    }

    @Test
    void updateOrderStatus_buyerCannotConfirm_throwsException() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));

        assertThrows(InvalidStatusTransitionException.class,
                () -> orderService.updateOrderStatus(orderId, buyerId, "CONFIRMED"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void updateOrderStatus_invalidTransition_throwsException() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));

        assertThrows(InvalidStatusTransitionException.class,
                () -> orderService.updateOrderStatus(orderId, buyerId, "DELIVERED"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void updateOrderStatus_invalidStatus_throwsException() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));

        assertThrows(InvalidStatusTransitionException.class,
                () -> orderService.updateOrderStatus(orderId, buyerId, "INVALID"));
    }

    @Test
    void updateOrderStatus_orderNotFound_throwsException() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.updateOrderStatus(orderId, buyerId, "CONFIRMED"));
    }

    @Test
    void updateOrderStatus_unauthorized_throwsException() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));

        assertThrows(UnauthorizedException.class,
                () -> orderService.updateOrderStatus(orderId, "stranger@example.com", "CONFIRMED"));
        verify(orderRepository, never()).save(any());
    }
}
