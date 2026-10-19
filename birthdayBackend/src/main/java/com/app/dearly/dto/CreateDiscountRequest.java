package com.app.dearly.dto;

public class CreateDiscountRequest {
    private String code;
    private String type;
    private double value;
    private Boolean active;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
