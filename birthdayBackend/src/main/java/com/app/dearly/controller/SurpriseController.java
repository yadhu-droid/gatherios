package com.app.dearly.controller;

import com.app.dearly.dto.*;
import com.app.dearly.service.OgCardService;
import com.app.dearly.service.PromotionService;
import com.app.dearly.service.SurpriseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class SurpriseController {

    private final SurpriseService surpriseService;
    private final OgCardService ogCardService;
    private final PromotionService promotionService;

    @Autowired
    public SurpriseController(SurpriseService surpriseService, OgCardService ogCardService, PromotionService promotionService) {
        this.surpriseService = surpriseService;
        this.ogCardService = ogCardService;
        this.promotionService = promotionService;
    }

    @PostMapping("/surprises")
    public ResponseEntity<SurpriseResponse> createSurprise(
            @RequestBody SurpriseRequest request,
            HttpServletRequest httpRequest
    ) {
        request.setClientUserAgent(httpRequest.getHeader("User-Agent"));
        request.setClientIpAddress(clientIp(httpRequest));
        SurpriseResponse response = surpriseService.createSurprise(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/surprises/{id}")
    public ResponseEntity<?> getSurpriseById(@PathVariable UUID id) {
        try {
            SurpriseResponse response = surpriseService.getSurpriseById(id);
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Serves the first invitation photo as a binary image for Open Graph / social previews.
     * Accepts data-URL or raw base64 stored in photo fields.
     */
    @GetMapping("/surprises/{id}/og-image")
    public ResponseEntity<byte[]> getOgImage(@PathVariable UUID id) {
        try {
            SurpriseResponse surprise = surpriseService.getSurpriseById(id);
            java.util.List<String> photos = surprise.getPhotos();
            if (photos == null || photos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            String raw = photos.get(0);
            if (raw == null || raw.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            String contentType = "image/jpeg";
            String payload = raw.trim();
            if (payload.startsWith("data:")) {
                int comma = payload.indexOf(',');
                if (comma < 0) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                }
                String header = payload.substring(5, comma).toLowerCase();
                if (header.contains("image/png")) contentType = "image/png";
                else if (header.contains("image/webp")) contentType = "image/webp";
                else if (header.contains("image/gif")) contentType = "image/gif";
                else contentType = "image/jpeg";
                payload = payload.substring(comma + 1);
            }

            byte[] bytes = java.util.Base64.getDecoder().decode(payload);
            // Social crawlers prefer reasonably sized images (~under a few MB)
            if (bytes.length == 0 || bytes.length > 5_000_000) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.ok()
                    .header("Cache-Control", "public, max-age=86400")
                    .header("Content-Type", contentType)
                    .body(bytes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Personalized 1200×630 Open Graph card (photo + names with heart + date + branding).
     */
    @GetMapping("/surprises/{id}/og-card")
    public ResponseEntity<byte[]> getOgCard(@PathVariable UUID id) {
        try {
            SurpriseResponse surprise = surpriseService.getSurpriseById(id);
            byte[] jpeg = ogCardService.renderJpeg(surprise);
            return ResponseEntity.ok()
                    .header("Cache-Control", "public, max-age=3600")
                    .header("Content-Type", "image/jpeg")
                    .body(jpeg);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/surprises/{id}/view")
    public ResponseEntity<?> incrementViews(@PathVariable UUID id) {
        try {
            surpriseService.incrementViews(id);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PatchMapping("/surprises/{id}/accept")
    public ResponseEntity<?> markAccepted(@PathVariable UUID id) {
        try {
            surpriseService.markAccepted(id);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        // Static credentials check for admin:
        if ("admin@123".equals(request.getUsername()) && "Abc123!@#".equals(request.getPassword())) {
            return ResponseEntity.ok(new LoginResponse(true, "mock-admin-token-12345", "Login successful"));
        }

        // Influencer / promo partner login
        Optional<InfluencerResponse> influencerOpt = promotionService.loginInfluencer(
                request.getUsername(), request.getPassword());
        if (influencerOpt.isPresent()) {
            String token = "mock-influencer-token-" + influencerOpt.get().getId();
            return ResponseEntity.ok(new LoginResponse(true, token, "Login successful"));
        }
        
        // Dynamic credentials check for wedding invitation owner:
        Optional<SurpriseResponse> surpriseOpt = surpriseService.getSurpriseByCredentials(
                request.getUsername(), request.getPassword());
        if (surpriseOpt.isPresent()) {
            String userToken = "mock-user-token-" + surpriseOpt.get().getId();
            return ResponseEntity.ok(new LoginResponse(true, userToken, "Login successful"));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new LoginResponse(false, null, "Invalid username or password"));
    }

    @GetMapping("/analytics")
    public ResponseEntity<?> getAnalytics(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized dashboard access");
        }

        if (authHeader.equals("Bearer mock-admin-token-12345")) {
            AnalyticsResponse response = surpriseService.getAnalytics();
            return ResponseEntity.ok(response);
        } else if (authHeader.startsWith("Bearer mock-user-token-")) {
            try {
                String idStr = authHeader.substring("Bearer mock-user-token-".length());
                UUID id = UUID.fromString(idStr);
                AnalyticsResponse response = surpriseService.getAnalyticsForSurprise(id);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid owner session token");
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized dashboard access");
    }

    @PostMapping("/surprises/{id}/rsvp")
    public ResponseEntity<?> submitRsvp(@PathVariable UUID id, @RequestBody RsvpRequest request) {
        try {
            surpriseService.submitRsvp(id, request);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/surprises/{id}/custom-music/unlock")
    public ResponseEntity<?> unlockCustomMusic(
            @PathVariable UUID id,
            @RequestBody UnlockCustomMusicRequest request
    ) {
        try {
            return ResponseEntity.ok(surpriseService.unlockCustomMusic(id, request));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/surprises/{id}")
    public ResponseEntity<?> updateSurprise(@PathVariable UUID id, @RequestBody SurpriseRequest request) {
        try {
            SurpriseResponse response = surpriseService.updateSurprise(id, request);
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/surprises/{id}/wishes")
    public ResponseEntity<?> submitWish(@PathVariable UUID id, @RequestBody GuestWishRequest request) {
        try {
            surpriseService.submitWish(id, request);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PatchMapping("/wishes/{wishId}/toggle-approval")
    public ResponseEntity<?> toggleWishApproval(@PathVariable Long wishId) {
        try {
            surpriseService.toggleWishApproval(wishId);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PatchMapping("/surprises/{id}/wishwall-visibility")
    public ResponseEntity<?> toggleWishWallVisibility(@PathVariable UUID id, @RequestParam boolean visible) {
        try {
            surpriseService.toggleWishWallVisibility(id, visible);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/location-search")
    public ResponseEntity<?> searchLocation(@RequestParam("q") String query) {
        try {
            String url = "https://nominatim.openstreetmap.org/search?format=json&limit=5&countrycodes=in&q="
                    + java.net.URLEncoder.encode(query, "UTF-8");
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .header("User-Agent", "Gatherios/1.0 (contact@gatherios.com)")
                    .GET()
                    .build();
            java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            return ResponseEntity.ok().header("Content-Type", "application/json").body(response.body());
        } catch (Exception e) {
            return ResponseEntity.ok("[]");
        }
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
