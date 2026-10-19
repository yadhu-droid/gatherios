package com.app.dearly.dao;

import com.app.dearly.entity.Rsvp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface RsvpDao extends JpaRepository<Rsvp, Long> {
    List<Rsvp> findBySurpriseId(UUID surpriseId);
}
