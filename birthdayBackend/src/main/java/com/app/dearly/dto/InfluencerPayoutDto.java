package com.app.dearly.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class InfluencerPayoutDto {
    private Long id;
    private UUID influencerId;
    private String influencerName;
    private String influencerEmail;
    private double amount;
    private double balanceAfter;
    private String note;
    private LocalDateTime createdAt;

    public InfluencerPayoutDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UUID getInfluencerId() { return influencerId; }
    public void setInfluencerId(UUID influencerId) { this.influencerId = influencerId; }

    public String getInfluencerName() { return influencerName; }
    public void setInfluencerName(String influencerName) { this.influencerName = influencerName; }

    public String getInfluencerEmail() { return influencerEmail; }
    public void setInfluencerEmail(String influencerEmail) { this.influencerEmail = influencerEmail; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public double getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(double balanceAfter) { this.balanceAfter = balanceAfter; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
