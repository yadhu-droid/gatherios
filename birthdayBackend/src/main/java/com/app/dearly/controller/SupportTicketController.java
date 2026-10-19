package com.app.dearly.controller;

import com.app.dearly.dto.SupportTicketRequest;
import com.app.dearly.dto.SupportTicketResponse;
import com.app.dearly.dto.UpdateTicketStatusRequest;
import com.app.dearly.service.SupportTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/support-tickets")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PATCH, RequestMethod.PUT, RequestMethod.OPTIONS})
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    @Autowired
    public SupportTicketController(SupportTicketService supportTicketService) {
        this.supportTicketService = supportTicketService;
    }

    @PostMapping
    public ResponseEntity<SupportTicketResponse> createTicket(@RequestBody SupportTicketRequest request) {
        SupportTicketResponse response = supportTicketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<SupportTicketResponse>> getAllTickets(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) UUID surpriseId) {
        if (email != null && !email.trim().isEmpty()) {
            return ResponseEntity.ok(supportTicketService.getTicketsByEmail(email.trim()));
        }
        if (surpriseId != null) {
            return ResponseEntity.ok(supportTicketService.getTicketsBySurpriseId(surpriseId));
        }
        return ResponseEntity.ok(supportTicketService.getAllTickets());
    }

    @GetMapping("/surprise/{surpriseId}")
    public ResponseEntity<List<SupportTicketResponse>> getTicketsBySurpriseId(@PathVariable UUID surpriseId) {
        return ResponseEntity.ok(supportTicketService.getTicketsBySurpriseId(surpriseId));
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<List<SupportTicketResponse>> getTicketsByEmail(@PathVariable String email) {
        return ResponseEntity.ok(supportTicketService.getTicketsByEmail(email));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateTicketStatus(
            @PathVariable Long id,
            @RequestBody UpdateTicketStatusRequest request) {
        try {
            SupportTicketResponse response = supportTicketService.updateTicketStatus(id, request.getStatus());
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
