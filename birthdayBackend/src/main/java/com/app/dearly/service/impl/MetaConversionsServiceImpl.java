package com.app.dearly.service.impl;

import com.app.dearly.dto.SurpriseRequest;
import com.app.dearly.entity.Surprise;
import com.app.dearly.service.MetaConversionsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class MetaConversionsServiceImpl implements MetaConversionsService {

    private static final Logger log = LoggerFactory.getLogger(MetaConversionsServiceImpl.class);

    private final RestTemplate restTemplate;
    private final String pixelId;
    private final String accessToken;
    private final String apiVersion;

    public MetaConversionsServiceImpl(
            @Value("${meta.pixel.id:}") String pixelId,
            @Value("${meta.capi.access-token:}") String accessToken,
            @Value("${meta.capi.api-version:v21.0}") String apiVersion
    ) {
        this.pixelId = pixelId == null ? "" : pixelId.trim();
        this.accessToken = accessToken == null ? "" : accessToken.trim();
        this.apiVersion = (apiVersion == null || apiVersion.isBlank()) ? "v21.0" : apiVersion.trim();
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(4000);
        factory.setReadTimeout(8000);
        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    @Async
    public void sendPurchaseAfterPayment(SurpriseRequest request, Surprise saved) {
        if (pixelId.isBlank() || accessToken.isBlank() || request == null || saved == null) return;
        if (!isPaidPurchase(request)) return;

        String eventId = trimToNull(request.getMetaEventId());
        if (eventId == null) {
            log.warn("Skipping Meta CAPI Purchase: missing event_id");
            return;
        }

        double value = request.getSaleAmount() != null ? request.getSaleAmount() : 0;
        if (value <= 0) return;

        long eventTime = Instant.now().getEpochSecond();
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("event_name", "Purchase");
        event.put("event_time", eventTime);
        event.put("event_id", eventId);
        event.put("action_source", "website");
        String sourceUrl = trimToNull(request.getMetaEventSourceUrl());
        if (sourceUrl != null) {
            event.put("event_source_url", sourceUrl);
        }

        Map<String, Object> userData = new LinkedHashMap<>();
        addHashed(userData, "em", normalizeEmail(request.getUsername()));
        addHashed(userData, "fn", normalizeName(request.getFirstName()));
        addHashed(userData, "ln", normalizeName(request.getLastName()));
        String ua = trimToNull(request.getClientUserAgent());
        if (ua != null) {
            userData.put("client_user_agent", ua);
        }
        String ip = trimToNull(request.getClientIpAddress());
        if (ip != null) {
            userData.put("client_ip_address", ip);
        }
        String fbp = trimToNull(request.getFbp());
        if (fbp != null) userData.put("fbp", fbp);
        String fbc = trimToNull(request.getFbc());
        if (fbc != null) userData.put("fbc", fbc);
        event.put("user_data", userData);

        Map<String, Object> custom = new LinkedHashMap<>();
        custom.put("currency", "INR");
        custom.put("value", String.format(Locale.US, "%.2f", value));
        custom.put("content_type", "product");
        custom.put("content_ids", List.of(String.valueOf(saved.getTemplate())));
        if (saved.getName() != null && !saved.getName().isBlank()) {
            custom.put("content_name", saved.getName().trim());
        }
        event.put("custom_data", custom);

        Map<String, Object> original = new LinkedHashMap<>();
        original.put("event_name", "Purchase");
        original.put("event_time", eventTime);
        event.put("original_event_data", original);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("data", List.of(event));
        body.put("access_token", accessToken);

        String url = "https://graph.facebook.com/" + apiVersion + "/" + pixelId + "/events";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
        } catch (Exception e) {
            log.warn("Meta CAPI Purchase failed: {}", e.getMessage());
        }
    }

    private boolean isPaidPurchase(SurpriseRequest request) {
        String paymentId = trimToNull(request.getRazorpayPaymentId());
        if (paymentId != null) return true;
        String orderId = trimToNull(request.getRazorpayOrderId());
        if (orderId == null || "free".equalsIgnoreCase(orderId)) return false;
        return request.getSaleAmount() != null && request.getSaleAmount() > 0;
    }

    private void addHashed(Map<String, Object> userData, String key, String normalized) {
        if (normalized == null || normalized.isBlank()) return;
        String hash = sha256Hex(normalized);
        if (hash == null) return;
        List<String> values = new ArrayList<>();
        values.add(hash);
        userData.put(key, values);
    }

    private String normalizeEmail(String raw) {
        String value = trimToNull(raw);
        if (value == null || !value.contains("@")) return null;
        return value.toLowerCase(Locale.ROOT);
    }

    /** Meta: lowercase, no punctuation, no spaces. Do not hash empty results. */
    private String normalizeName(String raw) {
        String value = trimToNull(raw);
        if (value == null) return null;
        String cleaned = value.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}]", "");
        return cleaned.isBlank() ? null : cleaned;
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hashed.length * 2);
            for (byte b : hashed) {
                String part = Integer.toHexString(0xff & b);
                if (part.length() == 1) hex.append('0');
                hex.append(part);
            }
            return hex.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
