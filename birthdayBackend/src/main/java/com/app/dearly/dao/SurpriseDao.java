package com.app.dearly.dao;

import com.app.dearly.entity.Surprise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SurpriseDao extends JpaRepository<Surprise, UUID> {
    Optional<Surprise> findByUsernameAndPassword(String username, String password);
    Optional<Surprise> findFirstByUsernameAndPasswordOrderByCreatedAtDesc(String username, String password);
    List<Surprise> findByUsernameOrderByCreatedAtDesc(String username);
    Optional<Surprise> findFirstByUsernameOrderByCreatedAtDesc(String username);

    /**
     * Admin dashboard list — excludes photo/music TEXT blobs so PostgreSQL
     * and JSON serialization stay fast even with many invitations.
     */
    @Query("""
        select s.id, s.recipient, s.occasion, s.name, s.template, s.createdAt, s.views,
               s.accepted, s.acceptedAt, s.username, s.password, s.razorpayOrderId, s.razorpayPaymentId,
               s.moneyGiftEnabled, s.moneyGiftPlan, s.moneyGiftUpi, s.moneyGiftPlatformFeePercent,
               s.giftPrice, s.giftDebt, s.customMusicEnabled, s.showWishWall
        from Surprise s
        order by s.createdAt desc
        """)
    List<Object[]> findAnalyticsRows();
}
