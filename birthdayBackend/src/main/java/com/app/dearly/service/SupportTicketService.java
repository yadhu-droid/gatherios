package com.app.dearly.service;

import com.app.dearly.dto.SupportTicketRequest;
import com.app.dearly.dto.SupportTicketResponse;

import java.util.List;
import java.util.UUID;

public interface SupportTicketService {
    SupportTicketResponse createTicket(SupportTicketRequest request);
    List<SupportTicketResponse> getAllTickets();
    List<SupportTicketResponse> getTicketsByEmail(String email);
    List<SupportTicketResponse> getTicketsBySurpriseId(UUID surpriseId);
    SupportTicketResponse updateTicketStatus(Long ticketId, String status);
}
