package com.example.ecommerce.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    public enum Status {
        CREATED,
        PAID,
        FAILED,
        CANCELLED
    }

    @Id
    private String id;

    private String userId;

    private Double totalAmount;

    private Status status;

    private Instant createdAt;
}
