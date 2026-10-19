package com.app.dearly.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "promo_conversions")
public class PromoConversion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "influencer_id", nullable = false)
    private UUID influencerId;

    @Column(name = "promo_link_id", nullable = false)
    private UUID promoLinkId;

    @Column(name = "surprise_id", nullable = false)
    private UUID surpriseId;

    @Column(name = "razorpay_order_id")
    private String razorpayOrderId;

    @Column(name = "template_id", nullable = false)
    private int templateId;

    /** Sale amount in INR (rupees) */
    @Column(nullable = false)
    private double saleAmount;

    /** PERCENT or FLAT at time of sale */
    @Column(nullable = false, length = 10)
    private String commissionType;

    @Column(nullable = false)
    private double commissionValue;

    @Column(nullable = false)
    private double commissionAmount;

    @Column(nullable = false)
    private boolean paidOut = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public PromoConversion() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UUID getInfluencerId() { return influencerId; }
    public void setInfluencerId(UUID influencerId) { this.influencerId = influencerId; }

    public UUID getPromoLinkId() { return promoLinkId; }
    public void setPromoLinkId(UUID promoLinkId) { this.promoLinkId = promoLinkId; }

    public UUID getSurpriseId() { return surpriseId; }
    public void setSurpriseId(UUID surpriseId) { this.surpriseId = surpriseId; }

    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }

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
