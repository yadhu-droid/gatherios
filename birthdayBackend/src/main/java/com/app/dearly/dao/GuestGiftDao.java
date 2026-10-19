package com.app.dearly.dao;

import com.app.dearly.entity.GuestGift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GuestGiftDao extends JpaRepository<GuestGift, Long> {
    List<GuestGift> findBySurpriseIdOrderByCreatedAtDesc(UUID surpriseId);
    List<GuestGift> findByStatusOrderByCreatedAtDesc(String status);
    Optional<GuestGift> findByRazorpayOrderId(String razorpayOrderId);
}
