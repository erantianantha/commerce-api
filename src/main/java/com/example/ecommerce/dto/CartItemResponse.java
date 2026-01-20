package com.example.ecommerce.dto;

import com.example.ecommerce.model.Product;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {
    private String id;
    private String productId;
    private Integer quantity;
    private ProductSummary product;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductSummary {
        private String id;
        private String name;
        private Double price;
    }
}
