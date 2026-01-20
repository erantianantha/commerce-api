package com.example.ecommerce.service;

import com.example.ecommerce.dto.AddToCartRequest;
import com.example.ecommerce.dto.CartItemResponse;
import com.example.ecommerce.exception.ApiException;
import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.CartItemRepository;
import com.example.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartItem addToCart(AddToCartRequest req) {
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));

        if (product.getStock() == null || product.getStock() <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Product out of stock");
        }
        if (req.getQuantity() > product.getStock()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Not enough stock available");
        }

        CartItem item = cartItemRepository.findByUserIdAndProductId(req.getUserId(), req.getProductId())
                .map(existing -> {
                    int newQty = existing.getQuantity() + req.getQuantity();
                    if (newQty > product.getStock()) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot exceed available stock");
                    }
                    existing.setQuantity(newQty);
                    return existing;
                })
                .orElseGet(() -> CartItem.builder()
                        .userId(req.getUserId())
                        .productId(req.getProductId())
                        .quantity(req.getQuantity())
                        .build());

        return cartItemRepository.save(item);
    }

    public List<CartItemResponse> getCart(String userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);
        List<CartItemResponse> out = new ArrayList<>();

        for (CartItem it : items) {
            Product p = productRepository.findById(it.getProductId()).orElse(null);
            CartItemResponse.ProductSummary summary = null;
            if (p != null) {
                summary = CartItemResponse.ProductSummary.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .price(p.getPrice())
                        .build();
            }
            out.add(CartItemResponse.builder()
                    .id(it.getId())
                    .productId(it.getProductId())
                    .quantity(it.getQuantity())
                    .product(summary)
                    .build());
        }

        return out;
    }

    public void clearCart(String userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    public List<CartItem> getCartItemsRaw(String userId) {
        return cartItemRepository.findByUserId(userId);
    }
}
