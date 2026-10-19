package com.app.dearly.dto;

public class ApplyDiscountRequest {
    private String code;
    /** Original cart total in rupees. */
    private double amount;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}
