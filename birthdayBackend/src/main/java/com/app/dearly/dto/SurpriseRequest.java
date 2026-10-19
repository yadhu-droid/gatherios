package com.app.dearly.dto;

import java.util.List;

public class SurpriseRequest {
    private String recipient;
    private String occasion;
    private String name;
    private String birthdate;
    private String promptVal;
    private String message;
    private List<String> photos;
    private int template;

    // Getters and Setters
    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getOccasion() {
        return occasion;
    }

    public void setOccasion(String occasion) {
        this.occasion = occasion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getPromptVal() {
        return promptVal;
    }

    public void setPromptVal(String promptVal) {
        this.promptVal = promptVal;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getPhotos() {
        return photos;
    }

    public void setPhotos(List<String> photos) {
        this.photos = photos;
    }

    public int getTemplate() {
        return template;
    }

    public void setTemplate(int template) {
        this.template = template;
    }

    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private String razorpayOrderId;
    private String razorpayPaymentId;

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
    }

    public String getRazorpayPaymentId() {
        return razorpayPaymentId;
    }

    public void setRazorpayPaymentId(String razorpayPaymentId) {
        this.razorpayPaymentId = razorpayPaymentId;
    }

    private Boolean showWishWall = true;

    public Boolean isShowWishWall() {
        return showWishWall == null ? true : showWishWall;
    }

    public void setShowWishWall(Boolean showWishWall) {
        this.showWishWall = showWishWall;
    }

    private Boolean customMusicEnabled = false;
    private String customMusicData;
    private String customMusicName;
    private Boolean moneyGiftEnabled = false;
    private String moneyGiftPlan = "OFF";
    private String moneyGiftUpi;
    private Double moneyGiftPlatformFeePercent = 0.0;

    public Boolean isCustomMusicEnabled() {
        return customMusicEnabled != null && customMusicEnabled;
    }

    public void setCustomMusicEnabled(Boolean customMusicEnabled) {
        this.customMusicEnabled = customMusicEnabled;
    }

    public String getCustomMusicData() {
        return customMusicData;
    }

    public void setCustomMusicData(String customMusicData) {
        this.customMusicData = customMusicData;
    }

    public String getCustomMusicName() {
        return customMusicName;
    }

    public void setCustomMusicName(String customMusicName) {
        this.customMusicName = customMusicName;
    }

    public Boolean isMoneyGiftEnabled() {
        return moneyGiftEnabled != null && moneyGiftEnabled;
    }

    public void setMoneyGiftEnabled(Boolean moneyGiftEnabled) {
        this.moneyGiftEnabled = moneyGiftEnabled;
    }

    public String getMoneyGiftPlan() {
        return moneyGiftPlan == null ? "OFF" : moneyGiftPlan;
    }

    public void setMoneyGiftPlan(String moneyGiftPlan) {
        this.moneyGiftPlan = moneyGiftPlan;
    }

    public String getMoneyGiftUpi() {
        return moneyGiftUpi;
    }

    public void setMoneyGiftUpi(String moneyGiftUpi) {
        this.moneyGiftUpi = moneyGiftUpi;
    }

    public Double getMoneyGiftPlatformFeePercent() {
        return moneyGiftPlatformFeePercent == null ? 0.0 : moneyGiftPlatformFeePercent;
    }

    public void setMoneyGiftPlatformFeePercent(Double moneyGiftPlatformFeePercent) {
        this.moneyGiftPlatformFeePercent = moneyGiftPlatformFeePercent;
    }

    /** Optional influencer referral code captured from ?ref= */
    private String promoCode;

    /** Paid amount in INR, used when the payments row is not visible yet. */
    private Double saleAmount;

    public Double getSaleAmount() {
        return saleAmount;
    }

    public void setSaleAmount(Double saleAmount) {
        this.saleAmount = saleAmount;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    /** Pixel / CAPI de-duplication id for Purchase. */
    private String metaEventId;
    private String metaEventSourceUrl;
    private String firstName;
    private String lastName;
    private String fbp;
    private String fbc;
    private String clientUserAgent;
    private String clientIpAddress;

    public String getMetaEventId() { return metaEventId; }
    public void setMetaEventId(String metaEventId) { this.metaEventId = metaEventId; }
    public String getMetaEventSourceUrl() { return metaEventSourceUrl; }
    public void setMetaEventSourceUrl(String metaEventSourceUrl) { this.metaEventSourceUrl = metaEventSourceUrl; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getFbp() { return fbp; }
    public void setFbp(String fbp) { this.fbp = fbp; }
    public String getFbc() { return fbc; }
    public void setFbc(String fbc) { this.fbc = fbc; }
    public String getClientUserAgent() { return clientUserAgent; }
    public void setClientUserAgent(String clientUserAgent) { this.clientUserAgent = clientUserAgent; }
    public String getClientIpAddress() { return clientIpAddress; }
    public void setClientIpAddress(String clientIpAddress) { this.clientIpAddress = clientIpAddress; }
}
