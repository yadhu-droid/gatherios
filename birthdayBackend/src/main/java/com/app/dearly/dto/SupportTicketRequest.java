package com.app.dearly.dto;

import java.util.UUID;

public class SupportTicketRequest {

    private UUID surpriseId;
    private String email;
    private Integer template;
    private String templateName;
    private String subject;
    private String message;
    private String password;

    public SupportTicketRequest() {}

    public UUID getSurpriseId() {
        return surpriseId;
    }

    public void setSurpriseId(UUID surpriseId) {
        this.surpriseId = surpriseId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getTemplate() {
        return template;
    }

    public void setTemplate(Integer template) {
        this.template = template;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
