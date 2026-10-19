package com.app.dearly.dao;

import com.app.dearly.entity.Influencer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InfluencerDao extends JpaRepository<Influencer, UUID> {
    Optional<Influencer> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}
