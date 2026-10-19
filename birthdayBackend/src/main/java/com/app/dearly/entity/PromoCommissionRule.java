package com.app.dearly.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "promo_commission_rules",
    uniqueConstraints = @UniqueConstraint(columnNames = {"scope", "promo_link_id", "template_id"})
)
public class PromoCommissionRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** GLOBAL or LINK */
    @Column(nullable = false, length = 10)
    private String scope;

    /** Null for GLOBAL rules */
    @Column(name = "promo_link_id")
    private UUID promoLinkId;

    @Column(name = "template_id", nullable = false)
    private int templateId;

    /** PERCENT or FLAT */
    @Column(nullable = false, length = 10)
    private String type;

    @Column(nullable = false)
    private double value;

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public PromoCommissionRule() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    public UUID getPromoLinkId() { return promoLinkId; }
    public void setPromoLinkId(UUID promoLinkId) { this.promoLinkId = promoLinkId; }

    public int getTemplateId() { return templateId; }
    public void setTemplateId(int templateId) { this.templateId = templateId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
