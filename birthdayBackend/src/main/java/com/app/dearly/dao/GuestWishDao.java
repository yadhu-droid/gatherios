package com.app.dearly.dao;

import com.app.dearly.entity.GuestWish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface GuestWishDao extends JpaRepository<GuestWish, Long> {
    List<GuestWish> findBySurpriseId(UUID surpriseId);
    List<GuestWish> findBySurpriseIdAndApproved(UUID surpriseId, boolean approved);
}
