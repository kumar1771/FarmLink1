package com.example.Order.Service.Repository;

import com.example.Order.Service.Model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByBuyerId(String buyerId, Pageable pageable);
    Page<Order> findByFarmerId(String farmerId, Pageable pageable);
}
