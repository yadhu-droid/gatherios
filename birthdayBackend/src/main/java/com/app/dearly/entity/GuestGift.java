package com.app.dearly.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "guest_gifts")
public class GuestGift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UUID surpriseId;

    private String guestName;

    /** Gross amount paid by guest (rupees) */
    private double amountTotal;

    /** Credited to the wedding creator (80% or 100%) */
    private double coupleAmount;

    /** Platform share / admin debt (20% or 0) */
    private double platformDebt;

    private double feePercent;

    /** PREMIUM | SHARE */
    @Column(length = 20)
    private String plan;

    private String razorpayOrderId;
    private String razorpayPaymentId;

    /** CREATED | PAID | FAILED */
    private String status = "CREATED";

    private LocalDateTime createdAt = LocalDateTime.now();

    public GuestGift() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UUID getSurpriseId() { return surpriseId; }
    public void setSurpriseId(UUID surpriseId) { this.surpriseId = surpriseId; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public double getAmountTotal() { return amountTotal; }
    public void setAmountTotal(double amountTotal) { this.amountTotal = amountTotal; }

    public double getCoupleAmount() { return coupleAmount; }
    public void setCoupleAmount(double coupleAmount) { this.coupleAmount = coupleAmount; }

    public double getPlatformDebt() { return platformDebt; }
    public void setPlatformDebt(double platformDebt) { this.platformDebt = platformDebt; }

    public double getFeePercent() { return feePercent; }
    public void setFeePercent(double feePercent) { this.feePercent = feePercent; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }

    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public void setRazorpayPaymentId(String razorpayPaymentId) { this.razorpayPaymentId = razorpayPaymentId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
