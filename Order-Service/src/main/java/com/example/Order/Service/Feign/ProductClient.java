package com.example.Order.Service.Feign;

import com.example.Order.Service.Dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "farmer-goods-service")
public interface ProductClient {

    @GetMapping("/products/{id}")
    ProductResponse getProduct(@PathVariable Long id);

    @PutMapping("/products/reduce-stock/{id}")
    void reduceStock(@PathVariable Long id,
                     @RequestParam int quantity);
}
