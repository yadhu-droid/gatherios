package com.app.dearly.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "rsvps")
public class Rsvp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "surprise_id", nullable = false)
    private UUID surpriseId;

    @Column(nullable = false)
    private String name;

    @Column(name = "guest_count", nullable = false)
    private int guestCount = 1;

    @Column(columnDefinition = "TEXT")
    private String wishes;

    private LocalDateTime createdAt = LocalDateTime.now();

    // Default Constructor
    public Rsvp() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getSurpriseId() {
        return surpriseId;
    }

    public void setSurpriseId(UUID surpriseId) {
        this.surpriseId = surpriseId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGuestCount() {
        return guestCount;
    }

    public void setGuestCount(int guestCount) {
        this.guestCount = guestCount;
    }

    public String getWishes() {
        return wishes;
    }

    public void setWishes(String wishes) {
        this.wishes = wishes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
