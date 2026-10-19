package com.app.dearly.dto;

public class UnlockCustomMusicRequest {
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
    private String customMusicData;
    private String customMusicName;

    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }
    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public void setRazorpayPaymentId(String razorpayPaymentId) { this.razorpayPaymentId = razorpayPaymentId; }
    public String getRazorpaySignature() { return razorpaySignature; }
    public void setRazorpaySignature(String razorpaySignature) { this.razorpaySignature = razorpaySignature; }
    public String getCustomMusicData() { return customMusicData; }
    public void setCustomMusicData(String customMusicData) { this.customMusicData = customMusicData; }
    public String getCustomMusicName() { return customMusicName; }
    public void setCustomMusicName(String customMusicName) { this.customMusicName = customMusicName; }
}
