package com.example.Farmer.Goods.Service.Service;

import com.example.Farmer.Goods.Service.Dto.ProductRequest;
import com.example.Farmer.Goods.Service.Exception.InsufficientStockException;
import com.example.Farmer.Goods.Service.Exception.ResourceNotFoundException;
import com.example.Farmer.Goods.Service.Exception.UnauthorizedException;
import com.example.Farmer.Goods.Service.Repository.ProductRepository;
import com.example.Farmer.Goods.Service.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public Product addProduct(String farmerId, ProductRequest request) {
        Product product = Product.builder()
                .farmerId(farmerId)
                .productName(request.getProductName())
                .category(request.getCategory())
                .type(request.getType())
                .farmerPrice(request.getFarmerPrice())
                .marketPrice(request.getMarketPrice())
                .quantityAvailable(request.getQuantityAvailable())
                .unit(request.getUnit())
                .location(request.getLocation())
                .harvestDate(request.getHarvestDate())
                .available(true)
                .build();
        log.info("Adding product '{}' for farmerId: {}", request.getProductName(), farmerId);
        return productRepository.save(product);
    }

    public List<Product> getFarmerProducts(String farmerId) {
        return productRepository.findByFarmerId(farmerId);
    }

    @Transactional
    public Product updateProduct(String farmerId, Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (!product.getFarmerId().equals(farmerId)) {
            throw new UnauthorizedException("Unauthorized action");
        }
        product.setProductName(request.getProductName());
        product.setCategory(request.getCategory());
        product.setType(request.getType());
        product.setFarmerPrice(request.getFarmerPrice());
        product.setMarketPrice(request.getMarketPrice());
        product.setQuantityAvailable(request.getQuantityAvailable());
        product.setUnit(request.getUnit());
        product.setLocation(request.getLocation());
        product.setHarvestDate(request.getHarvestDate());
        log.info("Updating product id: {} for farmerId: {}", id, farmerId);
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(String farmerId, Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (!product.getFarmerId().equals(farmerId)) {
            throw new UnauthorizedException("Unauthorized action");
        }
        log.info("Deleting product id: {} for farmerId: {}", id, farmerId);
        productRepository.deleteById(id);
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    @Transactional
    public void reduceStock(Long id, int quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (product.getQuantityAvailable() < quantity) {
            throw new InsufficientStockException("Not enough stock");
        }
        product.setQuantityAvailable(product.getQuantityAvailable() - quantity);
        productRepository.save(product);
        log.info("Reduced stock for product id: {} by {}", id, quantity);
    }
}
