package com.app.dearly.dao;

import com.app.dearly.entity.InfluencerBalance;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InfluencerBalanceDao extends JpaRepository<InfluencerBalance, Long> {
    Optional<InfluencerBalance> findByInfluencerId(UUID influencerId);

    boolean existsByInfluencerId(UUID influencerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from InfluencerBalance b where b.influencerId = :influencerId")
    Optional<InfluencerBalance> findByInfluencerIdForUpdate(@Param("influencerId") UUID influencerId);
}
