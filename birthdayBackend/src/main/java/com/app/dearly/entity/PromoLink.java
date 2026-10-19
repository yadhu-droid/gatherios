package com.app.dearly.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "promo_links")
public class PromoLink {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "influencer_id", nullable = false, unique = true)
    private UUID influencerId;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public PromoLink() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getInfluencerId() { return influencerId; }
    public void setInfluencerId(UUID influencerId) { this.influencerId = influencerId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
