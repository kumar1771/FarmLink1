package com.example.Order.Service.Kafka;

import com.example.Order.Service.Event.OrderPlacedEvent;
import com.example.Order.Service.Event.OrderStatusUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.order-placed}")
    private String orderPlacedTopic;

    @Value("${kafka.topic.order-status-updated}")
    private String orderStatusUpdatedTopic;

    public void publishOrderPlaced(OrderPlacedEvent event) {
        kafkaTemplate.send(orderPlacedTopic, event);
        log.info("Published order-placed event for orderId: {}", event.getOrderId());
    }

    public void publishOrderStatusUpdated(OrderStatusUpdatedEvent event) {
        kafkaTemplate.send(orderStatusUpdatedTopic, event);
        log.info("Published order-status-updated event for orderId: {}", event.getOrderId());
    }
}
