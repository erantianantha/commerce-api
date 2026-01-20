package com.example.ecommerce.service;

import com.example.ecommerce.exception.ApiException;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Product create(Product req) {
        if (req.getName() == null || req.getName().trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Product name is required");
        }
        if (req.getPrice() == null || req.getPrice() <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Price must be > 0");
        }
        if (req.getStock() == null || req.getStock() < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Stock must be >= 0");
        }
        return productRepository.save(req);
    }

    public List<Product> list() {
        return productRepository.findAll();
    }

    public List<Product> search(String q) {
        if (q == null || q.isBlank()) return list();
        return productRepository.findByNameContainingIgnoreCase(q);
    }

    public Product getByIdOrThrow(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found: " + productId));
    }
}
