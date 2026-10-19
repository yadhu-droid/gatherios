package com.app.dearly.dto;

import java.util.List;
import java.util.Map;

public class AnalyticsResponse {
    private long totalSurprises;
    private long totalViews;
    private double averageViews;
    private long totalAccepted;
    private double acceptanceRate;
    private Map<String, Long> occasionBreakdown;
    private Map<Integer, Long> templateBreakdown;
    private List<SurpriseResponse> surprisesList;

    // Getters and Setters
    public long getTotalSurprises() {
        return totalSurprises;
    }

    public void setTotalSurprises(long totalSurprises) {
        this.totalSurprises = totalSurprises;
    }

    public long getTotalViews() {
        return totalViews;
    }

    public void setTotalViews(long totalViews) {
        this.totalViews = totalViews;
    }

    public double getAverageViews() {
        return averageViews;
    }

    public void setAverageViews(double averageViews) {
        this.averageViews = averageViews;
    }

    public long getTotalAccepted() {
        return totalAccepted;
    }

    public void setTotalAccepted(long totalAccepted) {
        this.totalAccepted = totalAccepted;
    }

    public double getAcceptanceRate() {
        return acceptanceRate;
    }

    public void setAcceptanceRate(double acceptanceRate) {
        this.acceptanceRate = acceptanceRate;
    }

    public Map<String, Long> getOccasionBreakdown() {
        return occasionBreakdown;
    }

    public void setOccasionBreakdown(Map<String, Long> occasionBreakdown) {
        this.occasionBreakdown = occasionBreakdown;
    }

    public Map<Integer, Long> getTemplateBreakdown() {
        return templateBreakdown;
    }

    public void setTemplateBreakdown(Map<Integer, Long> templateBreakdown) {
        this.templateBreakdown = templateBreakdown;
    }

    public List<SurpriseResponse> getSurprisesList() {
        return surprisesList;
    }

    public void setSurprisesList(List<SurpriseResponse> surprisesList) {
        this.surprisesList = surprisesList;
    }

    private double totalPayouts;

    public double getTotalPayouts() {
        return totalPayouts;
    }

    public void setTotalPayouts(double totalPayouts) {
        this.totalPayouts = totalPayouts;
    }

    /** Sum of giftPrice across all invitations (couple credits) — separate from invitation payouts */
    private double totalGiftPrice;

    /** Sum of giftDebt across all invitations (platform SHARE cut) */
    private double totalGiftDebt;

    public double getTotalGiftPrice() {
        return totalGiftPrice;
    }

    public void setTotalGiftPrice(double totalGiftPrice) {
        this.totalGiftPrice = totalGiftPrice;
    }

    public double getTotalGiftDebt() {
        return totalGiftDebt;
    }

    public void setTotalGiftDebt(double totalGiftDebt) {
        this.totalGiftDebt = totalGiftDebt;
    }
}
