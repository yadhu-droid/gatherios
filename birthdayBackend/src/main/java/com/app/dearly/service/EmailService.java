package com.app.dearly.service;

public interface EmailService {
    void sendDashboardCredentialsEmail(String toEmail, String generatedPassword, String coupleName, String surpriseId);
}
