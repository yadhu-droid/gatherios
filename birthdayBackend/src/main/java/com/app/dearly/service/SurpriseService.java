package com.app.dearly.service;

import com.app.dearly.dto.AnalyticsResponse;
import com.app.dearly.dto.SurpriseRequest;
import com.app.dearly.dto.SurpriseResponse;
import java.util.UUID;

public interface SurpriseService {
    SurpriseResponse createSurprise(SurpriseRequest request);
    SurpriseResponse getSurpriseById(UUID id);
    void incrementViews(UUID id);
    void markAccepted(UUID id);
    AnalyticsResponse getAnalytics();
    AnalyticsResponse getAnalyticsForSurprise(UUID id);
    java.util.Optional<SurpriseResponse> getSurpriseByCredentials(String username, String password);
    void submitRsvp(UUID id, com.app.dearly.dto.RsvpRequest request);
    SurpriseResponse updateSurprise(UUID id, SurpriseRequest request);
    SurpriseResponse unlockCustomMusic(UUID id, com.app.dearly.dto.UnlockCustomMusicRequest request);
    void submitWish(UUID id, com.app.dearly.dto.GuestWishRequest request);
    void toggleWishApproval(Long wishId);
    void toggleWishWallVisibility(UUID id, boolean visible);
}
