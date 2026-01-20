package com.example.ecommerce.dto;

import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.Payment;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private String id;
    private String userId;
    private Double totalAmount;
    private Order.Status status;
    private Instant createdAt;
    private List<Item> items;
    private PaymentInfo payment;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Item {
        private String productId;
        private Integer quantity;
        private Double price;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentInfo {
        private String id;
        private Payment.Status status;
        private Double amount;
        private String paymentId;
        private String razorpayOrderId;
    }
}
