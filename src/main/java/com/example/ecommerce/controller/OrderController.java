package com.example.ecommerce.controller;

import com.example.ecommerce.dto.CreateOrderRequest;
import com.example.ecommerce.dto.OrderResponse;
import com.example.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest req) {
        return orderService.createOrderFromCart(req);
    }

    @GetMapping("/{orderId}")
    public OrderResponse get(@PathVariable String orderId) {
        return orderService.getOrder(orderId);
    }

    @GetMapping("/user/{userId}")
    public List<OrderResponse> userOrders(@PathVariable String userId) {
        return orderService.getUserOrders(userId);
    }

    @PostMapping("/{orderId}/cancel")
    public Map<String, Object> cancel(@PathVariable String orderId) {
        orderService.cancelOrder(orderId);
        return Map.of("message", "Order cancelled");
    }
}
