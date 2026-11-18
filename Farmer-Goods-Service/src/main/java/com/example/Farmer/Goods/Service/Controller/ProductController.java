package com.example.Farmer.Goods.Service.Controller;

import com.example.Farmer.Goods.Service.Dto.ProductRequest;
import com.example.Farmer.Goods.Service.Service.ProductService;
import com.example.Farmer.Goods.Service.model.Product;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<Product> addProduct(
            @RequestHeader("X-User-Id") String farmerId,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.addProduct(farmerId, request));
    }

    @GetMapping("/my")
    public List<Product> getMyProducts(@RequestHeader("X-User-Id") String farmerId) {
        return productService.getFarmerProducts(farmerId);
    }

    @PutMapping("/{id}")
    public Product updateProduct(
            @RequestHeader("X-User-Id") String farmerId,
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return productService.updateProduct(farmerId, id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @RequestHeader("X-User-Id") String farmerId,
            @PathVariable Long id) {
        productService.deleteProduct(farmerId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public Page<Product> getAllProducts(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return productService.getAllProducts(pageable);
    }

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        return productService.getProduct(id);
    }

    @PutMapping("/reduce-stock/{id}")
    public ResponseEntity<Void> reduceStock(@PathVariable Long id, @RequestParam int quantity) {
        productService.reduceStock(id, quantity);
        return ResponseEntity.noContent().build();
    }
}
