package com.app.dearly.dao;

import com.app.dearly.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SupportTicketDao extends JpaRepository<SupportTicket, Long> {
    List<SupportTicket> findByEmailOrderByCreatedAtDesc(String email);
    List<SupportTicket> findBySurpriseIdOrderByCreatedAtDesc(UUID surpriseId);
    List<SupportTicket> findByStatusOrderByCreatedAtDesc(String status);
    List<SupportTicket> findAllByOrderByCreatedAtDesc();
}
