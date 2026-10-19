package com.app.dearly.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InfluencerDashboardResponse {
    private UUID influencerId;
    private String name;
    private String email;
    private String promoCode;
    private String sharePath;
    private boolean active;
    private long totalPurchases;
    private double totalEarnings;
    private double unpaidEarnings;
    private double totalPaid;
    private List<ConversionDto> conversions = new ArrayList<>();
    private List<InfluencerPayoutDto> payouts = new ArrayList<>();

    public static class ConversionDto {
        private Long id;
        private UUID surpriseId;
        private int templateId;
        private double saleAmount;
        private String commissionType;
        private double commissionValue;
        private double commissionAmount;
        private boolean paidOut;
        private LocalDateTime createdAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public UUID getSurpriseId() { return surpriseId; }
        public void setSurpriseId(UUID surpriseId) { this.surpriseId = surpriseId; }
        public int getTemplateId() { return templateId; }
        public void setTemplateId(int templateId) { this.templateId = templateId; }
        public double getSaleAmount() { return saleAmount; }
        public void setSaleAmount(double saleAmount) { this.saleAmount = saleAmount; }
        public String getCommissionType() { return commissionType; }
        public void setCommissionType(String commissionType) { this.commissionType = commissionType; }
        public double getCommissionValue() { return commissionValue; }
        public void setCommissionValue(double commissionValue) { this.commissionValue = commissionValue; }
        public double getCommissionAmount() { return commissionAmount; }
        public void setCommissionAmount(double commissionAmount) { this.commissionAmount = commissionAmount; }
        public boolean isPaidOut() { return paidOut; }
        public void setPaidOut(boolean paidOut) { this.paidOut = paidOut; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public UUID getInfluencerId() { return influencerId; }
    public void setInfluencerId(UUID influencerId) { this.influencerId = influencerId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPromoCode() { return promoCode; }
    public void setPromoCode(String promoCode) { this.promoCode = promoCode; }
    public String getSharePath() { return sharePath; }
    public void setSharePath(String sharePath) { this.sharePath = sharePath; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public long getTotalPurchases() { return totalPurchases; }
    public void setTotalPurchases(long totalPurchases) { this.totalPurchases = totalPurchases; }
    public double getTotalEarnings() { return totalEarnings; }
    public void setTotalEarnings(double totalEarnings) { this.totalEarnings = totalEarnings; }
    public double getUnpaidEarnings() { return unpaidEarnings; }
    public void setUnpaidEarnings(double unpaidEarnings) { this.unpaidEarnings = unpaidEarnings; }
    public double getTotalPaid() { return totalPaid; }
    public void setTotalPaid(double totalPaid) { this.totalPaid = totalPaid; }
    public List<ConversionDto> getConversions() { return conversions; }
    public void setConversions(List<ConversionDto> conversions) { this.conversions = conversions; }
    public List<InfluencerPayoutDto> getPayouts() { return payouts; }
    public void setPayouts(List<InfluencerPayoutDto> payouts) { this.payouts = payouts; }
}
