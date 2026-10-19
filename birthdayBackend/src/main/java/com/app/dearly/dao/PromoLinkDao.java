package com.app.dearly.dao;

import com.app.dearly.entity.PromoLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PromoLinkDao extends JpaRepository<PromoLink, UUID> {
    Optional<PromoLink> findByInfluencerId(UUID influencerId);
    Optional<PromoLink> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
}
