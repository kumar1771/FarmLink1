package com.example.Farmer.Goods.Service.Service;

import com.example.Farmer.Goods.Service.Dto.ProductRequest;
import com.example.Farmer.Goods.Service.Exception.InsufficientStockException;
import com.example.Farmer.Goods.Service.Exception.ResourceNotFoundException;
import com.example.Farmer.Goods.Service.Exception.UnauthorizedException;
import com.example.Farmer.Goods.Service.Repository.ProductRepository;
import com.example.Farmer.Goods.Service.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @InjectMocks private ProductService productService;

    private Product existingProduct;
    private ProductRequest request;
    private final String farmerId      = "farmer@example.com";
    private final String otherFarmerId = "other@example.com";
    private final Long productId       = 1L;

    @BeforeEach
    void setUp() {
        request = new ProductRequest();
        request.setProductName("Tomatoes");
        request.setCategory("Vegetables");
        request.setType("organic");
        request.setFarmerPrice(2.50);
        request.setMarketPrice(4.00);
        request.setQuantityAvailable(100);
        request.setUnit("kg");
        request.setLocation("Yorkshire");
        request.setHarvestDate(LocalDate.now());

        existingProduct = Product.builder()
                .id(productId)
                .farmerId(farmerId)
                .productName("Tomatoes")
                .category("Vegetables")
                .type("organic")
                .farmerPrice(2.50)
                .marketPrice(4.00)
                .quantityAvailable(100)
                .unit("kg")
                .location("Yorkshire")
                .available(true)
                .build();
    }

    // ─── addProduct ───────────────────────────────────────────────────────────

    @Test
    void addProduct_success() {
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        Product result = productService.addProduct(farmerId, request);

        assertEquals(farmerId, result.getFarmerId());
        assertEquals("Tomatoes", result.getProductName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    // ─── getFarmerProducts ────────────────────────────────────────────────────

    @Test
    void getFarmerProducts_returnsList() {
        when(productRepository.findByFarmerId(farmerId)).thenReturn(List.of(existingProduct));

        List<Product> result = productService.getFarmerProducts(farmerId);

        assertEquals(1, result.size());
        assertEquals(farmerId, result.get(0).getFarmerId());
    }

    @Test
    void getFarmerProducts_noProducts_returnsEmptyList() {
        when(productRepository.findByFarmerId(farmerId)).thenReturn(List.of());

        assertTrue(productService.getFarmerProducts(farmerId).isEmpty());
    }

    // ─── getProduct ───────────────────────────────────────────────────────────

    @Test
    void getProduct_success() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));

        Product result = productService.getProduct(productId);

        assertEquals(productId, result.getId());
    }

    @Test
    void getProduct_notFound_throwsException() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.getProduct(productId));
    }

    // ─── getAllProducts ───────────────────────────────────────────────────────

    @Test
    void getAllProducts_returnsList() {
        PageRequest pageable = PageRequest.of(0, 20);
        Page<Product> page = new PageImpl<>(List.of(existingProduct));
        when(productRepository.findAll(pageable)).thenReturn(page);

        assertEquals(1, productService.getAllProducts(pageable).getTotalElements());
    }

    // ─── updateProduct ────────────────────────────────────────────────────────

    @Test
    void updateProduct_success() {
        request.setProductName("Updated Tomatoes");
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        Product result = productService.updateProduct(farmerId, productId, request);

        assertEquals("Updated Tomatoes", result.getProductName());
        verify(productRepository, times(1)).save(existingProduct);
    }

    @Test
    void updateProduct_notFound_throwsException() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.updateProduct(farmerId, productId, request));
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_wrongFarmer_throwsException() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));

        assertThrows(UnauthorizedException.class,
                () -> productService.updateProduct(otherFarmerId, productId, request));
        verify(productRepository, never()).save(any());
    }

    // ─── deleteProduct ────────────────────────────────────────────────────────

    @Test
    void deleteProduct_success() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));

        productService.deleteProduct(farmerId, productId);

        verify(productRepository, times(1)).deleteById(productId);
    }

    @Test
    void deleteProduct_notFound_throwsException() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.deleteProduct(farmerId, productId));
        verify(productRepository, never()).deleteById(any());
    }

    @Test
    void deleteProduct_wrongFarmer_throwsException() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));

        assertThrows(UnauthorizedException.class,
                () -> productService.deleteProduct(otherFarmerId, productId));
        verify(productRepository, never()).deleteById(any());
    }

    // ─── reduceStock ──────────────────────────────────────────────────────────

    @Test
    void reduceStock_success() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));

        productService.reduceStock(productId, 10);

        assertEquals(90, existingProduct.getQuantityAvailable());
        verify(productRepository, times(1)).save(existingProduct);
    }

    @Test
    void reduceStock_insufficientStock_throwsException() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));

        assertThrows(InsufficientStockException.class,
                () -> productService.reduceStock(productId, 200));
        verify(productRepository, never()).save(any());
    }

    @Test
    void reduceStock_productNotFound_throwsException() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.reduceStock(productId, 10));
    }
}
