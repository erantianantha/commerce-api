package com.example.ecommerce.controller;

import com.example.ecommerce.model.Product;
import com.example.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public Product create(@RequestBody Product req) {
        return productService.create(req);
    }

    @GetMapping
    public List<Product> list() {
        return productService.list();
    }

    @GetMapping("/search")
    public List<Product> search(@RequestParam(name = "q", required = false) String q) {
        return productService.search(q);
    }
}
