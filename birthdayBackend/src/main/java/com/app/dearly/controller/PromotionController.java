package com.app.dearly.controller;

import com.app.dearly.dto.*;
import com.app.dearly.service.PromotionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/promotions")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {
        RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH,
        RequestMethod.DELETE, RequestMethod.OPTIONS
})
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    private boolean isAdmin(String authHeader) {
        return authHeader != null && authHeader.equals("Bearer mock-admin-token-12345");
    }

    private UUID influencerIdFromAuth(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer mock-influencer-token-")) return null;
        try {
            return UUID.fromString(authHeader.substring("Bearer mock-influencer-token-".length()));
        } catch (Exception e) {
            return null;
        }
    }

    @GetMapping("/influencers")
    public ResponseEntity<?> listInfluencers(@RequestHeader(value = "Authorization", required = false) String auth) {
        if (!isAdmin(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        return ResponseEntity.ok(promotionService.listInfluencers());
    }

    @PostMapping("/influencers")
    public ResponseEntity<?> createInfluencer(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody CreateInfluencerRequest request
    ) {
        if (!isAdmin(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(promotionService.createInfluencer(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PatchMapping("/influencers/{id}/profile")
    public ResponseEntity<?> updateInfluencerProfile(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable UUID id,
            @RequestBody UpdateInfluencerProfileRequest request
    ) {
        if (!isAdmin(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        try {
            return ResponseEntity.ok(promotionService.updateInfluencerProfile(id, request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @PatchMapping("/influencers/{id}/status")
    public ResponseEntity<?> updateInfluencerStatus(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable UUID id,
            @RequestBody Map<String, Boolean> body
    ) {
        if (!isAdmin(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        try {
            boolean active = Boolean.TRUE.equals(body.get("active"));
            return ResponseEntity.ok(promotionService.updateInfluencerStatus(id, active));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PatchMapping("/influencers/{id}/link-status")
    public ResponseEntity<?> updateLinkStatus(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable UUID id,
            @RequestBody Map<String, Boolean> body
    ) {
        if (!isAdmin(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        try {
            boolean active = Boolean.TRUE.equals(body.get("active"));
            return ResponseEntity.ok(promotionService.updatePromoLinkStatus(id, active));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/rules/global")
    public ResponseEntity<?> listGlobalRules(@RequestHeader(value = "Authorization", required = false) String auth) {
        if (!isAdmin(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        return ResponseEntity.ok(promotionService.listGlobalRules());
    }

    @PutMapping("/rules/global")
    public ResponseEntity<?> upsertGlobalRule(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody UpsertCommissionRuleRequest request
    ) {
        if (!isAdmin(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        try {
            request.setScope("GLOBAL");
            return ResponseEntity.ok(promotionService.upsertGlobalRule(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping({"/rules/global", "/rules/global/{templateId}"})
    public ResponseEntity<?> deleteGlobalRule(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable(required = false) Integer templateId
    ) {
        if (!isAdmin(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        promotionService.deleteGlobalRule();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/influencers/{id}/overrides")
    public ResponseEntity<?> listOverrides(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable UUID id
    ) {
        if (!isAdmin(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        try {
            return ResponseEntity.ok(promotionService.listLinkOverrides(id));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/influencers/{id}/overrides")
    public ResponseEntity<?> upsertOverride(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable UUID id,
            @RequestBody UpsertCommissionRuleRequest request
    ) {
        if (!isAdmin(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        try {
            request.setScope("LINK");
            return ResponseEntity.ok(promotionService.upsertLinkOverride(id, request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping({"/influencers/{id}/overrides", "/influencers/{id}/overrides/{templateId}"})
    public ResponseEntity<?> deleteOverride(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable UUID id,
            @PathVariable(required = false) Integer templateId
    ) {
        if (!isAdmin(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        try {
            promotionService.deleteLinkOverride(id);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> myDashboard(@RequestHeader(value = "Authorization", required = false) String auth) {
        UUID influencerId = influencerIdFromAuth(auth);
        if (influencerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        try {
            return ResponseEntity.ok(promotionService.getInfluencerDashboard(influencerId));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/me/payouts")
    public ResponseEntity<?> myPayouts(@RequestHeader(value = "Authorization", required = false) String auth) {
        UUID influencerId = influencerIdFromAuth(auth);
        if (influencerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        try {
            return ResponseEntity.ok(promotionService.listInfluencerPayouts(influencerId));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/payments")
    public ResponseEntity<?> listPaymentBalances(@RequestHeader(value = "Authorization", required = false) String auth) {
        if (!isAdmin(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        return ResponseEntity.ok(promotionService.listInfluencerPaymentBalances());
    }

    @PostMapping("/influencers/{id}/payouts")
    public ResponseEntity<?> recordPayout(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable UUID id,
            @RequestBody RecordInfluencerPayoutRequest request
    ) {
        if (!isAdmin(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        try {
            return ResponseEntity.ok(promotionService.recordInfluencerPayout(id, request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/influencers/{id}/payouts")
    public ResponseEntity<?> listPayoutsForInfluencer(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable UUID id
    ) {
        if (!isAdmin(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        try {
            return ResponseEntity.ok(promotionService.listInfluencerPayouts(id));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
