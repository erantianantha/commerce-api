package com.example.ecommerce.webhook;

import com.example.ecommerce.dto.RazorpayWebhookPayload;
import com.example.ecommerce.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class PaymentWebhookController {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${razorpay.webhookSecret}")
    private String webhookSecret;

    @PostMapping("/payment")
    public ResponseEntity<Map<String, Object>> onPaymentWebhook(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature
    ) throws Exception {

        // Signature verification (recommended by Razorpay)
        if (StringUtils.hasText(webhookSecret) && StringUtils.hasText(signature)) {
            if (!verifySignature(rawBody, signature, webhookSecret)) {
                return ResponseEntity.status(400).body(Map.of("message", "Invalid signature"));
            }
        }

        RazorpayWebhookPayload payload = objectMapper.readValue(rawBody, RazorpayWebhookPayload.class);

        String event = payload.getEvent();
        String razorpayOrderId = null;
        String razorpayPaymentId = null;
        String paymentStatus = null;

        if (payload.getPayload() != null && payload.getPayload().getPayment() != null
                && payload.getPayload().getPayment().getEntity() != null) {
            razorpayPaymentId = payload.getPayload().getPayment().getEntity().getId();
            razorpayOrderId = payload.getPayload().getPayment().getEntity().getOrder_id();
            paymentStatus = payload.getPayload().getPayment().getEntity().getStatus();
        }

        // We only care about captured/failed
        if (razorpayOrderId != null) {
            if ("payment.captured".equalsIgnoreCase(event) || "captured".equalsIgnoreCase(paymentStatus)) {
                paymentService.markPaymentSuccess(razorpayOrderId, razorpayPaymentId);
            } else if ("payment.failed".equalsIgnoreCase(event) || "failed".equalsIgnoreCase(paymentStatus)) {
                paymentService.markPaymentFailed(razorpayOrderId, razorpayPaymentId);
            }
        }

        return ResponseEntity.ok(Map.of("message", "Webhook processed"));
    }

    private boolean verifySignature(String payload, String actualSignature, String secret) throws Exception {
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256Hmac.init(secretKey);
        byte[] hash = sha256Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        String expected = Base64.getEncoder().encodeToString(hash);

        // Razorpay sends signature in hex? Actually Razorpay signature is hex digest for webhook.
        // We'll support both hex and base64 comparisons to be safe in student environments.
        String expectedHex = toHex(hash);

        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), actualSignature.getBytes(StandardCharsets.UTF_8))
                || MessageDigest.isEqual(expectedHex.getBytes(StandardCharsets.UTF_8), actualSignature.getBytes(StandardCharsets.UTF_8));
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
