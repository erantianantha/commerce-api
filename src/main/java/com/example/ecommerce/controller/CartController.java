package com.example.ecommerce.controller;

import com.example.ecommerce.dto.AddToCartRequest;
import com.example.ecommerce.dto.CartItemResponse;
import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    public CartItem add(@Valid @RequestBody AddToCartRequest req) {
        return cartService.addToCart(req);
    }

    @GetMapping("/{userId}")
    public List<CartItemResponse> getCart(@PathVariable String userId) {
        return cartService.getCart(userId);
    }

    @DeleteMapping("/{userId}/clear")
    public Map<String, Object> clear(@PathVariable String userId) {
        cartService.clearCart(userId);
        return Map.of("message", "Cart cleared successfully");
    }
}
