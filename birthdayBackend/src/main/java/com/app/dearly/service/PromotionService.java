package com.app.dearly.service;

import com.app.dearly.dto.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PromotionService {
    List<InfluencerResponse> listInfluencers();
    InfluencerResponse createInfluencer(CreateInfluencerRequest request);
    InfluencerResponse updateInfluencerProfile(UUID id, UpdateInfluencerProfileRequest request);
    InfluencerResponse updateInfluencerStatus(UUID id, boolean active);
    InfluencerResponse updatePromoLinkStatus(UUID influencerId, boolean active);

    List<CommissionRuleDto> listGlobalRules();
    CommissionRuleDto upsertGlobalRule(UpsertCommissionRuleRequest request);
    void deleteGlobalRule();

    List<CommissionRuleDto> listLinkOverrides(UUID influencerId);
    CommissionRuleDto upsertLinkOverride(UUID influencerId, UpsertCommissionRuleRequest request);
    void deleteLinkOverride(UUID influencerId);

    Optional<InfluencerResponse> loginInfluencer(String email, String password);
    InfluencerDashboardResponse getInfluencerDashboard(UUID influencerId);

    void recordConversionIfEligible(UUID surpriseId, int templateId, String promoCode, String razorpayOrderId, Double saleAmountHint);

    List<InfluencerResponse> listInfluencerPaymentBalances();
    InfluencerPayoutDto recordInfluencerPayout(UUID influencerId, RecordInfluencerPayoutRequest request);
    List<InfluencerPayoutDto> listInfluencerPayouts(UUID influencerId);
}
