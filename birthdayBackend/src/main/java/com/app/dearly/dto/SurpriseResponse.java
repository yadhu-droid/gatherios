package com.app.dearly.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class SurpriseResponse {
    private UUID id;
    private String recipient;
    private String occasion;
    private String name;
    private String birthdate;
    private String promptVal;
    private String message;
    private List<String> photos;
    private int template;
    private LocalDateTime createdAt;
    private int views;
    private boolean accepted;
    private LocalDateTime acceptedAt;

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    private String username;
    private String password;
    private List<RsvpResponse> rsvps;

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

    private List<GuestWishResponse> guestWishes;

    public List<RsvpResponse> getRsvps() {
        return rsvps;
    }

    public void setRsvps(List<RsvpResponse> rsvps) {
        this.rsvps = rsvps;
    }

    public List<GuestWishResponse> getGuestWishes() {
        return guestWishes;
    }

    public void setGuestWishes(List<GuestWishResponse> guestWishes) {
        this.guestWishes = guestWishes;
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

    private double payoutAmount;

    public double getPayoutAmount() {
        return payoutAmount;
    }

    public void setPayoutAmount(double payoutAmount) {
        this.payoutAmount = payoutAmount;
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

    private Double giftPrice = 0.0;
    private Double giftDebt = 0.0;

    public Double getGiftPrice() {
        return giftPrice == null ? 0.0 : giftPrice;
    }

    public void setGiftPrice(Double giftPrice) {
        this.giftPrice = giftPrice;
    }

    public Double getGiftDebt() {
        return giftDebt == null ? 0.0 : giftDebt;
    }

    public void setGiftDebt(Double giftDebt) {
        this.giftDebt = giftDebt;
    }
}
