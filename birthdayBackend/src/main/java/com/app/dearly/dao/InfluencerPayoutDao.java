package com.app.dearly.dao;

import com.app.dearly.entity.InfluencerPayout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InfluencerPayoutDao extends JpaRepository<InfluencerPayout, Long> {
    List<InfluencerPayout> findByInfluencerIdOrderByCreatedAtDesc(UUID influencerId);
}
