package com.example.ecommerce.controller;

import com.example.ecommerce.dto.PaymentCreateRequest;
import com.example.ecommerce.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    public Map<String, Object> create(@Valid @RequestBody PaymentCreateRequest req) {
        return paymentService.createPayment(req);
    }
}
