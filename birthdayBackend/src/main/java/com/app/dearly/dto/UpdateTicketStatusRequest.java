package com.app.dearly.dto;

public class UpdateTicketStatusRequest {

    private String status;

    public UpdateTicketStatusRequest() {}

    public UpdateTicketStatusRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
