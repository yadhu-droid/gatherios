package com.app.dearly.controller;

import com.app.dearly.service.DiscountService;
import com.app.dearly.service.impl.PaymentRecordService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Autowired
    private PaymentRecordService paymentRecordService;

    @Autowired
    private DiscountService discountService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> request) {
        try {
            int originalPaise = toPaise(request.getOrDefault("amount", 29900));
            String discountCode = request.get("discountCode") == null ? "" : String.valueOf(request.get("discountCode"));
            int amount;
            try {
                amount = discountService.applyToPaise(originalPaise, discountCode);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
            if (amount > 0 && amount < 100) {
                amount = 100;
            }

            if (amount <= 0) {
                Map<String, Object> free = new HashMap<>();
                free.put("orderId", "free");
                free.put("amount", 0);
                free.put("currency", "INR");
                free.put("keyId", razorpayKeyId);
                free.put("free", true);
                return ResponseEntity.ok(free);
            }

            // Build Razorpay order request
            Map<String, Object> orderRequest = new HashMap<>();
            orderRequest.put("amount", amount);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "receipt_" + System.currentTimeMillis());

            // Set Basic Auth header
            String credentials = razorpayKeyId + ":" + razorpayKeySecret;
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic " + encodedCredentials);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(orderRequest, headers);

            // Call Razorpay Orders API
            ResponseEntity<String> razorpayResponse = restTemplate.postForEntity(
                    "https://api.razorpay.com/v1/orders",
                    entity,
                    String.class
            );

            JsonNode responseBody = objectMapper.readTree(razorpayResponse.getBody());
            String orderId = responseBody.get("id").asText();

            // Commit before the response so attribution can see amount + promo after checkout.
            String promoCode = request.get("promoCode") == null ? null : String.valueOf(request.get("promoCode"));
            String purpose = request.get("purpose") == null ? null : String.valueOf(request.get("purpose"));
            try {
                paymentRecordService.recordCreatedOrder(orderId, amount / 100.0, promoCode, purpose);
            } catch (Exception persistError) {
                log.warn("Could not store payment {} for promo attribution: {}", orderId, persistError.getMessage());
            }

            // Return order details to frontend
            Map<String, Object> result = new HashMap<>();
            result.put("orderId", orderId);
            result.put("amount", amount);
            result.put("currency", "INR");
            result.put("keyId", razorpayKeyId);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create Razorpay order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> request) {
        try {
            String razorpayOrderId = request.get("razorpayOrderId");
            String razorpayPaymentId = request.get("razorpayPaymentId");
            String razorpaySignature = request.get("razorpaySignature");

            // Generate expected signature using HMAC-SHA256
            String data = razorpayOrderId + "|" + razorpayPaymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    razorpayKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"
            );
            mac.init(secretKey);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            String expectedSignature = hexString.toString();

            boolean verified = expectedSignature.equals(razorpaySignature);

            if (verified) {
                try {
                    paymentRecordService.markPaid(razorpayOrderId, razorpayPaymentId, razorpaySignature);
                } catch (Exception persistError) {
                    log.warn("Could not mark payment {} paid: {}", razorpayOrderId, persistError.getMessage());
                }
            } else {
                try {
                    paymentRecordService.markFailed(razorpayOrderId);
                } catch (Exception persistError) {
                    log.warn("Could not mark payment {} failed: {}", razorpayOrderId, persistError.getMessage());
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("verified", verified);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Payment verification failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    private int toPaise(Object raw) {
        if (raw instanceof Number) {
            return ((Number) raw).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(raw));
        } catch (Exception e) {
            return 29900;
        }
    }

}
