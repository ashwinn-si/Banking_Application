package com.ashwinsi.bankingApplication.Domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy =  GenerationType.AUTO)
    private Long id;

    private UUID userId;

    private UUID accountId;

    private String action;

    private String method;

    private String endpoint;

    private LocalDateTime timestamp;

    private String details;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();

    }
}
