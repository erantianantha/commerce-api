package com.example.ecommerce.service;

import com.example.ecommerce.dto.CreateOrderRequest;
import com.example.ecommerce.dto.OrderResponse;
import com.example.ecommerce.exception.ApiException;
import com.example.ecommerce.model.*;
import com.example.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;

    public OrderResponse createOrderFromCart(CreateOrderRequest req) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(req.getUserId());
        if (cartItems.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        // validate stock and compute total
        double total = 0;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem ci : cartItems) {
            Product product = productRepository.findById(ci.getProductId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found: " + ci.getProductId()));

            if (ci.getQuantity() <= 0) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid quantity in cart");
            }
            if (product.getStock() < ci.getQuantity()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Insufficient stock for product: " + product.getName());
            }

            total += product.getPrice() * ci.getQuantity();
            orderItems.add(OrderItem.builder()
                    .productId(product.getId())
                    .quantity(ci.getQuantity())
                    .price(product.getPrice())
                    .build());
        }

        Order order = Order.builder()
                .userId(req.getUserId())
                .totalAmount(total)
                .status(Order.Status.CREATED)
                .createdAt(Instant.now())
                .build();
        order = orderRepository.save(order);

        // persist order items + reduce stock
        for (OrderItem oi : orderItems) {
            oi.setOrderId(order.getId());
            orderItemRepository.save(oi);

            Product p = productRepository.findById(oi.getProductId()).orElseThrow();
            p.setStock(p.getStock() - oi.getQuantity());
            productRepository.save(p);
        }

        // clear cart
        cartItemRepository.deleteByUserId(req.getUserId());

        return buildOrderResponse(order);
    }

    public OrderResponse getOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));
        return buildOrderResponse(order);
    }

    public List<OrderResponse> getUserOrders(String userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<OrderResponse> out = new ArrayList<>();
        for (Order o : orders) out.add(buildOrderResponse(o));
        return out;
    }

    public void cancelOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));

        if (order.getStatus() == Order.Status.PAID) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Paid orders cannot be cancelled");
        }
        if (order.getStatus() == Order.Status.CANCELLED) {
            return;
        }

        // restore stock
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        for (OrderItem it : items) {
            Product p = productRepository.findById(it.getProductId()).orElse(null);
            if (p != null) {
                p.setStock(p.getStock() + it.getQuantity());
                productRepository.save(p);
            }
        }

        order.setStatus(Order.Status.CANCELLED);
        orderRepository.save(order);

        // mark payment failed if exists
        paymentRepository.findByOrderId(orderId).ifPresent(pay -> {
            if (pay.getStatus() == Payment.Status.PENDING) {
                pay.setStatus(Payment.Status.FAILED);
                paymentRepository.save(pay);
            }
        });
    }

    private OrderResponse buildOrderResponse(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());

        List<OrderResponse.Item> respItems = new ArrayList<>();
        for (OrderItem it : items) {
            respItems.add(OrderResponse.Item.builder()
                    .productId(it.getProductId())
                    .quantity(it.getQuantity())
                    .price(it.getPrice())
                    .build());
        }

        OrderResponse.PaymentInfo paymentInfo = paymentRepository.findByOrderId(order.getId())
                .map(p -> OrderResponse.PaymentInfo.builder()
                        .id(p.getId())
                        .status(p.getStatus())
                        .amount(p.getAmount())
                        .paymentId(p.getPaymentId())
                        .razorpayOrderId(p.getRazorpayOrderId())
                        .build())
                .orElse(null);

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .items(respItems)
                .payment(paymentInfo)
                .build();
    }
}
