package com.example.ecommerce.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    public enum Status {
        PENDING,
        SUCCESS,
        FAILED
    }

    @Id
    private String id;

    private String orderId;

    private Double amount;

    private Status status;

    /**
     * External payment id. For Razorpay it's the `payment_id`.
     */
    private String paymentId;

    /**
     * Razorpay order_id returned when we create a Razorpay order.
     */
    private String razorpayOrderId;

    private Instant createdAt;
}
