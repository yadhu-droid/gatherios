package com.app.dearly.controller;

import com.app.dearly.dao.GuestGiftDao;
import com.app.dearly.dao.SurpriseDao;
import com.app.dearly.entity.GuestGift;
import com.app.dearly.entity.Surprise;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class GiftController {

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    private final SurpriseDao surpriseDao;
    private final GuestGiftDao guestGiftDao;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GiftController(SurpriseDao surpriseDao, GuestGiftDao guestGiftDao) {
        this.surpriseDao = surpriseDao;
        this.guestGiftDao = guestGiftDao;
    }

    @PostMapping("/surprises/{id}/gifts/order")
    public ResponseEntity<?> createGiftOrder(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        try {
            Surprise surprise = surpriseDao.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Invitation not found"));

            if (!Boolean.TRUE.equals(surprise.isMoneyGiftEnabled())
                    || "OFF".equalsIgnoreCase(surprise.getMoneyGiftPlan())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Gifts are not enabled for this invitation."));
            }

            double amountRupees = toDouble(body.get("amount"));
            if (amountRupees < 51) {
                return ResponseEntity.badRequest().body(Map.of("error", "Minimum gift amount is ₹51."));
            }
            if (amountRupees > 500000) {
                return ResponseEntity.badRequest().body(Map.of("error", "Gift amount is too large."));
            }

            String guestName = body.get("guestName") != null ? String.valueOf(body.get("guestName")).trim() : "Guest";
            if (guestName.isBlank()) guestName = "Guest";

            int amountPaise = (int) Math.round(amountRupees * 100);

            Map<String, Object> orderRequest = new HashMap<>();
            orderRequest.put("amount", amountPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "gift_" + id.toString().substring(0, 8) + "_" + System.currentTimeMillis());
            orderRequest.put("notes", Map.of(
                    "type", "wedding_gift",
                    "surpriseId", id.toString(),
                    "guestName", guestName
            ));

            String credentials = razorpayKeyId + ":" + razorpayKeySecret;
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic " + encodedCredentials);

            ResponseEntity<String> razorpayResponse = restTemplate.postForEntity(
                    "https://api.razorpay.com/v1/orders",
                    new HttpEntity<>(orderRequest, headers),
                    String.class
            );

            JsonNode responseBody = objectMapper.readTree(razorpayResponse.getBody());
            String orderId = responseBody.get("id").asText();

            String plan = surprise.getMoneyGiftPlan();
            double feePercent = "SHARE".equalsIgnoreCase(plan) ? 20.0 : 0.0;
            double platformDebt = round2(amountRupees * feePercent / 100.0);
            double coupleAmount = round2(amountRupees - platformDebt);

            GuestGift gift = new GuestGift();
            gift.setSurpriseId(id);
            gift.setGuestName(guestName);
            gift.setAmountTotal(amountRupees);
            gift.setCoupleAmount(coupleAmount);
            gift.setPlatformDebt(platformDebt);
            gift.setFeePercent(feePercent);
            gift.setPlan(plan);
            gift.setRazorpayOrderId(orderId);
            gift.setStatus("CREATED");
            gift.setCreatedAt(LocalDateTime.now());
            guestGiftDao.save(gift);

            return ResponseEntity.ok(Map.of(
                    "orderId", orderId,
                    "amount", amountPaise,
                    "currency", "INR",
                    "keyId", razorpayKeyId,
                    "coupleAmount", coupleAmount,
                    "platformDebt", platformDebt
            ));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create gift order: " + e.getMessage()));
        }
    }

    @PostMapping("/surprises/{id}/gifts/confirm")
    @Transactional
    public ResponseEntity<?> confirmGift(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        try {
            String orderId = body.get("razorpayOrderId");
            String paymentId = body.get("razorpayPaymentId");
            String signature = body.get("razorpaySignature");

            if (orderId == null || paymentId == null || signature == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing payment details."));
            }

            if (!verifySignature(orderId, paymentId, signature)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("verified", false));
            }

            Surprise surprise = surpriseDao.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Invitation not found"));

            GuestGift gift = guestGiftDao.findByRazorpayOrderId(orderId)
                    .orElseThrow(() -> new NoSuchElementException("Gift order not found"));

            if (!gift.getSurpriseId().equals(id)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Gift does not belong to this invitation."));
            }

            if ("PAID".equals(gift.getStatus())) {
                return ResponseEntity.ok(Map.of(
                        "verified", true,
                        "alreadyProcessed", true,
                        "coupleAmount", gift.getCoupleAmount(),
                        "platformDebt", gift.getPlatformDebt()
                ));
            }

            gift.setRazorpayPaymentId(paymentId);
            gift.setStatus("PAID");
            guestGiftDao.save(gift);

            surprise.setGiftPrice(round2(surprise.getGiftPrice() + gift.getCoupleAmount()));
            surprise.setGiftDebt(round2(surprise.getGiftDebt() + gift.getPlatformDebt()));
            surpriseDao.save(surprise);

            return ResponseEntity.ok(Map.of(
                    "verified", true,
                    "coupleAmount", gift.getCoupleAmount(),
                    "platformDebt", gift.getPlatformDebt(),
                    "giftPrice", surprise.getGiftPrice(),
                    "giftDebt", surprise.getGiftDebt()
            ));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to confirm gift: " + e.getMessage()));
        }
    }

    @GetMapping("/surprises/{id}/gifts")
    public ResponseEntity<?> listGifts(@PathVariable UUID id) {
        List<GuestGift> gifts = guestGiftDao.findBySurpriseIdOrderByCreatedAtDesc(id);
        return ResponseEntity.ok(gifts);
    }

    private boolean verifySignature(String orderId, String paymentId, String signature) throws Exception {
        String data = orderId + "|" + paymentId;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(razorpayKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            String h = Integer.toHexString(0xff & b);
            if (h.length() == 1) hex.append('0');
            hex.append(h);
        }
        return hex.toString().equals(signature);
    }

    private static double toDouble(Object value) {
        if (value == null) return 0;
        if (value instanceof Number n) return n.doubleValue();
        return Double.parseDouble(String.valueOf(value));
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
