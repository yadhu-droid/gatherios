package com.app.dearly.dto;

public class UpdateInfluencerProfileRequest {
    private String name;
    private String notes;
    private String upiId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getUpiId() { return upiId; }
    public void setUpiId(String upiId) { this.upiId = upiId; }
}
