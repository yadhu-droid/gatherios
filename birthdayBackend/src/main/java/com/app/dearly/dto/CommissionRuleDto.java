package com.app.dearly.dto;

import java.util.UUID;

public class CommissionRuleDto {
    private Long id;
    private String scope;
    private UUID promoLinkId;
    private int templateId;
    private String type;
    private double value;

    public CommissionRuleDto() {}

    public CommissionRuleDto(Long id, String scope, UUID promoLinkId, int templateId, String type, double value) {
        this.id = id;
        this.scope = scope;
        this.promoLinkId = promoLinkId;
        this.templateId = templateId;
        this.type = type;
        this.value = value;
    }

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
}
