package com.app.dearly.dao;

import com.app.dearly.entity.PromoConversion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PromoConversionDao extends JpaRepository<PromoConversion, Long> {
    List<PromoConversion> findByInfluencerIdOrderByCreatedAtDesc(UUID influencerId);
    boolean existsBySurpriseId(UUID surpriseId);
    long countByInfluencerId(UUID influencerId);

    @Query("select coalesce(sum(c.commissionAmount), 0) from PromoConversion c where c.influencerId = :influencerId")
    double sumCommissionByInfluencerId(@Param("influencerId") UUID influencerId);
}
