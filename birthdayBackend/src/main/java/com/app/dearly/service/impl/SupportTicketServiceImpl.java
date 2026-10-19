package com.app.dearly.service.impl;

import com.app.dearly.dao.SupportTicketDao;
import com.app.dearly.dao.SurpriseDao;
import com.app.dearly.dto.SupportTicketRequest;
import com.app.dearly.dto.SupportTicketResponse;
import com.app.dearly.entity.SupportTicket;
import com.app.dearly.entity.Surprise;
import com.app.dearly.service.SupportTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class SupportTicketServiceImpl implements SupportTicketService {

    private final SupportTicketDao supportTicketDao;
    private final SurpriseDao surpriseDao;

    @Autowired
    public SupportTicketServiceImpl(SupportTicketDao supportTicketDao, SurpriseDao surpriseDao) {
        this.supportTicketDao = supportTicketDao;
        this.surpriseDao = surpriseDao;
    }

    @Override
    public SupportTicketResponse createTicket(SupportTicketRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Owner email is required to raise a support ticket.");
        }
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            throw new IllegalArgumentException("Ticket message / issue description is required.");
        }

        SupportTicket ticket = new SupportTicket();
        ticket.setSurpriseId(request.getSurpriseId());
        ticket.setEmail(request.getEmail().trim());
        ticket.setTemplate(request.getTemplate());
        ticket.setTemplateName(request.getTemplateName());
        ticket.setSubject((request.getSubject() != null && !request.getSubject().trim().isEmpty())
                ? request.getSubject().trim() : "Support Inquiry");
        ticket.setMessage(request.getMessage().trim());
        ticket.setStatus("OPEN");

        // Automatically resolve and save user password
        String password = (request.getPassword() != null && !request.getPassword().trim().isEmpty())
                ? request.getPassword().trim() : null;

        if (password == null && request.getSurpriseId() != null) {
            password = surpriseDao.findById(request.getSurpriseId())
                    .map(Surprise::getPassword)
                    .orElse(null);
        }

        if (password == null) {
            password = surpriseDao.findFirstByUsernameOrderByCreatedAtDesc(request.getEmail().trim())
                    .map(Surprise::getPassword)
                    .orElse(null);
        }

        ticket.setPassword(password);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());

        SupportTicket saved = supportTicketDao.save(ticket);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketResponse> getAllTickets() {
        return supportTicketDao.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketResponse> getTicketsByEmail(String email) {
        return supportTicketDao.findByEmailOrderByCreatedAtDesc(email.trim())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketResponse> getTicketsBySurpriseId(UUID surpriseId) {
        return supportTicketDao.findBySurpriseIdOrderByCreatedAtDesc(surpriseId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SupportTicketResponse updateTicketStatus(Long ticketId, String status) {
        SupportTicket ticket = supportTicketDao.findById(ticketId)
                .orElseThrow(() -> new NoSuchElementException("Support ticket not found with ID: " + ticketId));

        String sanitized = (status != null && !status.trim().isEmpty()) ? status.trim().toUpperCase() : "RESOLVED";
        ticket.setStatus(sanitized);
        ticket.setUpdatedAt(LocalDateTime.now());

        SupportTicket updated = supportTicketDao.save(ticket);
        return mapToResponse(updated);
    }

    private SupportTicketResponse mapToResponse(SupportTicket ticket) {
        SupportTicketResponse resp = new SupportTicketResponse();
        resp.setId(ticket.getId());
        resp.setSurpriseId(ticket.getSurpriseId());
        resp.setEmail(ticket.getEmail());
        resp.setTemplate(ticket.getTemplate());
        resp.setTemplateName(ticket.getTemplateName());
        resp.setSubject(ticket.getSubject());
        resp.setMessage(ticket.getMessage());
        resp.setStatus(ticket.getStatus());

        // Password mapping with fallback for legacy tickets
        String pwd = ticket.getPassword();
        if (pwd == null || pwd.trim().isEmpty()) {
            if (ticket.getSurpriseId() != null) {
                pwd = surpriseDao.findById(ticket.getSurpriseId())
                        .map(Surprise::getPassword)
                        .orElse(null);
            }
            if (pwd == null || pwd.trim().isEmpty()) {
                pwd = surpriseDao.findFirstByUsernameOrderByCreatedAtDesc(ticket.getEmail().trim())
                        .map(Surprise::getPassword)
                        .orElse(null);
            }
        }
        resp.setPassword(pwd);

        resp.setCreatedAt(ticket.getCreatedAt());
        resp.setUpdatedAt(ticket.getUpdatedAt());
        return resp;
    }
}
