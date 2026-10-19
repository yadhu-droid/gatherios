package com.app.dearly.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InfluencerResponse {
    private UUID id;
    private String name;
    private String email;
    private String password;
    private boolean active;
    private String notes;
    private String upiId;
    private LocalDateTime createdAt;
    private UUID promoLinkId;
    private String promoCode;
    private boolean promoActive;
    private String sharePath;
    private long conversionCount;
    private double totalEarnings;
    private double unpaidEarnings;
    private double totalPaid;
    private List<CommissionRuleDto> linkOverrides = new ArrayList<>();
    private boolean alreadyExisted;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getUpiId() { return upiId; }
    public void setUpiId(String upiId) { this.upiId = upiId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public UUID getPromoLinkId() { return promoLinkId; }
    public void setPromoLinkId(UUID promoLinkId) { this.promoLinkId = promoLinkId; }
    public String getPromoCode() { return promoCode; }
    public void setPromoCode(String promoCode) { this.promoCode = promoCode; }
    public boolean isPromoActive() { return promoActive; }
    public void setPromoActive(boolean promoActive) { this.promoActive = promoActive; }
    public String getSharePath() { return sharePath; }
    public void setSharePath(String sharePath) { this.sharePath = sharePath; }
    public long getConversionCount() { return conversionCount; }
    public void setConversionCount(long conversionCount) { this.conversionCount = conversionCount; }
    public double getTotalEarnings() { return totalEarnings; }
    public void setTotalEarnings(double totalEarnings) { this.totalEarnings = totalEarnings; }
    public double getUnpaidEarnings() { return unpaidEarnings; }
    public void setUnpaidEarnings(double unpaidEarnings) { this.unpaidEarnings = unpaidEarnings; }
    public double getTotalPaid() { return totalPaid; }
    public void setTotalPaid(double totalPaid) { this.totalPaid = totalPaid; }
    public List<CommissionRuleDto> getLinkOverrides() { return linkOverrides; }
    public void setLinkOverrides(List<CommissionRuleDto> linkOverrides) { this.linkOverrides = linkOverrides; }
    public boolean isAlreadyExisted() { return alreadyExisted; }
    public void setAlreadyExisted(boolean alreadyExisted) { this.alreadyExisted = alreadyExisted; }
}
