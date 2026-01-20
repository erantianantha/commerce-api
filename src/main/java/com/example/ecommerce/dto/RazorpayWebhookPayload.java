package com.example.ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RazorpayWebhookPayload {

    private String event;

    private Payload payload;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Payload {
        @JsonProperty("payment")
        private PaymentEntity payment;

        @JsonProperty("order")
        private OrderEntity order;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaymentEntity {
        @JsonProperty("entity")
        private PaymentDetails entity;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaymentDetails {
        private String id;
        private String order_id;
        private String status;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrderEntity {
        @JsonProperty("entity")
        private OrderDetails entity;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrderDetails {
        private String id;
        private String status;
    }
}
