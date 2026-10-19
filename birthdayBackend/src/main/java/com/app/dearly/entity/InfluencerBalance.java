package com.app.dearly.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "influencer_balances",
    uniqueConstraints = @UniqueConstraint(columnNames = {"influencer_id"})
)
public class InfluencerBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "influencer_id", nullable = false, unique = true)
    private UUID influencerId;

    /** Outstanding commission still owed to the influencer (INR). */
    @Column(name = "unpaid_amount", nullable = false)
    private double unpaidAmount = 0;

    /** Lifetime payouts recorded against this influencer (INR). */
    @Column(name = "total_paid_amount", nullable = false)
    private double totalPaidAmount = 0;

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public InfluencerBalance() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UUID getInfluencerId() { return influencerId; }
    public void setInfluencerId(UUID influencerId) { this.influencerId = influencerId; }

    public double getUnpaidAmount() { return unpaidAmount; }
    public void setUnpaidAmount(double unpaidAmount) { this.unpaidAmount = unpaidAmount; }

    public double getTotalPaidAmount() { return totalPaidAmount; }
    public void setTotalPaidAmount(double totalPaidAmount) { this.totalPaidAmount = totalPaidAmount; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
