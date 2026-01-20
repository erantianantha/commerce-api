package com.example.ecommerce.service;

import com.example.ecommerce.dto.PaymentCreateRequest;
import com.example.ecommerce.exception.ApiException;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.Payment;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.PaymentRepository;
import com.razorpay.OrderClient;
import com.razorpay.RazorpayClient;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final RazorpayClient razorpayClient;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    public Map<String, Object> createPayment(PaymentCreateRequest req) {
        Order order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));

        if (order.getStatus() != Order.Status.CREATED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Payment can be created only for CREATED orders");
        }

        // if payment already exists, return it (idempotent behaviour)
        Payment existing = paymentRepository.findByOrderId(order.getId()).orElse(null);
        if (existing != null && existing.getStatus() == Payment.Status.PENDING) {
            return Map.of(
                    "paymentId", existing.getPaymentId(),
                    "orderId", existing.getOrderId(),
                    "amount", existing.getAmount(),
                    "status", existing.getStatus(),
                    "razorpayOrderId", existing.getRazorpayOrderId()
            );
        }

        double amount = req.getAmount();
        if (amount <= 0) throw new ApiException(HttpStatus.BAD_REQUEST, "Amount must be > 0");

        // Razorpay expects amount in smallest currency unit
        long amountPaise = Math.round(amount * 100);

        try {
            JSONObject options = new JSONObject();
            options.put("amount", amountPaise);
            options.put("currency", "INR");
            options.put("receipt", "rcpt_" + order.getId());
            options.put("payment_capture", 1);

            com.razorpay.Order rzpOrder = razorpayClient.orders.create(options);
            String razorpayOrderId = rzpOrder.get("id");

            Payment payment = Payment.builder()
                    .orderId(order.getId())
                    .amount(amount)
                    .status(Payment.Status.PENDING)
                    .razorpayOrderId(razorpayOrderId)
                    .createdAt(Instant.now())
                    .build();
            payment = paymentRepository.save(payment);

            return Map.of(
                    "paymentId", payment.getId(),
                    "orderId", payment.getOrderId(),
                    "amount", payment.getAmount(),
                    "status", payment.getStatus(),
                    "razorpayOrderId", payment.getRazorpayOrderId(),
                    "razorpayKey", "<use frontend>"
            );
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create Razorpay order: " + e.getMessage());
        }
    }

    public void markPaymentSuccess(String razorpayOrderId, String razorpayPaymentId) {
        Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Payment not found for razorpay_order_id"));

        payment.setStatus(Payment.Status.SUCCESS);
        payment.setPaymentId(razorpayPaymentId);
        paymentRepository.save(payment);

        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));

        order.setStatus(Order.Status.PAID);
        orderRepository.save(order);
    }

    public void markPaymentFailed(String razorpayOrderId, String razorpayPaymentId) {
        Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Payment not found for razorpay_order_id"));

        payment.setStatus(Payment.Status.FAILED);
        payment.setPaymentId(razorpayPaymentId);
        paymentRepository.save(payment);

        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));

        order.setStatus(Order.Status.FAILED);
        orderRepository.save(order);
    }
}
