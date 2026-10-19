package com.app.dearly.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "surprises")
public class Surprise {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String recipient;

    @Column(nullable = false)
    private String occasion;

    @Column(nullable = false)
    private String name;

    private String birthdate;

    private String promptVal;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(columnDefinition = "TEXT")
    private String photo1;

    @Column(columnDefinition = "TEXT")
    private String photo2;

    @Column(columnDefinition = "TEXT")
    private String photo3;

    @Column(columnDefinition = "TEXT")
    private String photo4;

    @Column(columnDefinition = "TEXT")
    private String photo5;

    @Column(columnDefinition = "TEXT")
    private String photo6;

    @Column(columnDefinition = "TEXT")
    private String photo7;

    @Column(columnDefinition = "TEXT")
    private String photo8;

    @Column(columnDefinition = "TEXT")
    private String photo9;

    @Column(columnDefinition = "TEXT")
    private String photo10;

    private int template;

    private LocalDateTime createdAt = LocalDateTime.now();

    private int views = 0;

    private boolean accepted = false;

    private LocalDateTime acceptedAt;

    private Boolean showWishWall = true;

    // Default Constructor
    public Surprise() {}

    public Boolean isShowWishWall() {
        return showWishWall == null ? true : showWishWall;
    }

    public void setShowWishWall(Boolean showWishWall) {
        this.showWishWall = showWishWall;
    }

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

    public String getPhoto1() { return photo1; }
    public void setPhoto1(String photo1) { this.photo1 = photo1; }

    public String getPhoto2() { return photo2; }
    public void setPhoto2(String photo2) { this.photo2 = photo2; }

    public String getPhoto3() { return photo3; }
    public void setPhoto3(String photo3) { this.photo3 = photo3; }

    public String getPhoto4() { return photo4; }
    public void setPhoto4(String photo4) { this.photo4 = photo4; }

    public String getPhoto5() { return photo5; }
    public void setPhoto5(String photo5) { this.photo5 = photo5; }

    public String getPhoto6() { return photo6; }
    public void setPhoto6(String photo6) { this.photo6 = photo6; }

    public String getPhoto7() { return photo7; }
    public void setPhoto7(String photo7) { this.photo7 = photo7; }

    public String getPhoto8() { return photo8; }
    public void setPhoto8(String photo8) { this.photo8 = photo8; }

    public String getPhoto9() { return photo9; }
    public void setPhoto9(String photo9) { this.photo9 = photo9; }

    public String getPhoto10() { return photo10; }
    public void setPhoto10(String photo10) { this.photo10 = photo10; }

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
    private String razorpayOrderId;
    private String razorpayPaymentId;

    /** Pro: couple uploaded their own invitation soundtrack */
    private Boolean customMusicEnabled = false;

    @Column(columnDefinition = "TEXT")
    private String customMusicData;

    private String customMusicName;

    /** Pro: guests can send monetary gifts via UPI */
    private Boolean moneyGiftEnabled = false;

    /** OFF | PREMIUM (₹1000, 0% fee) | SHARE (free, 20% platform fee) */
    @Column(length = 20)
    private String moneyGiftPlan = "OFF";

    private String moneyGiftUpi;

    /** Platform fee percent applied on guest gifts (0 for PREMIUM, 20 for SHARE) */
    private Double moneyGiftPlatformFeePercent = 0.0;

    /** Cumulative gift amount credited to the couple (rupees) */
    private Double giftPrice = 0.0;

    /** Cumulative platform share / admin debt from SHARE plan gifts (rupees) */
    private Double giftDebt = 0.0;

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
