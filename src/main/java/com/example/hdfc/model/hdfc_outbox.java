package com.example.hdfc.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "hdfc_outbox",
        indexes = {
                @Index(name = "idx_outbox_status", columnList = "status"),
                @Index(name = "idx_outbox_created_at", columnList = "createdAt")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class hdfc_outbox {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // 🔹 Link to Transaction
    @Column(nullable = false)
    private UUID transactionId;

    // 🔹 Type of Event
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;

    // 🔹 JSON Payload to send to NPCI
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    // 🔹 Delivery Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status;

    // 🔹 Retry Control
    @Column(nullable = false)
    private int retryCount = 0;

    private String lastErrorMessage;

    // 🔹 Audit
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime processedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum EventType {
        PAYMENT_INITIATED,
        PAYMENT_REVERSAL
    }

    public enum OutboxStatus {
        PENDING,
        PROCESSING,
        PROCESSED,
        FAILED
    }
}
