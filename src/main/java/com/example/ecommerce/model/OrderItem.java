package com.example.ecommerce.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    private String id;

    private String orderId;

    private String productId;

    private Integer quantity;

    /**
     * Price locked at the time of order.
     */
    private Double price;
}
