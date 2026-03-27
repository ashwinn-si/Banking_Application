package com.ashwinsi.bankingApplication.Domain;

import com.ashwinsi.bankingApplication.DTO.Enum.OtpStatusEnum;
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
public class Otp {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    //this action id will be generated for every action like for transaction the transaction id will use as action id
    private UUID actionId;

    private String action;

    private Integer otp;

    private String receiverEmail;

    private UUID userId;

    private Integer incorrectAttempts = 0;

    private Integer attemptsAllowed;

    @Enumerated(EnumType.STRING)
    private OtpStatusEnum status = OtpStatusEnum.OTP_CREATED;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Otp(UUID actionId, String action, Integer otp, String receiverEmail, UUID userId, Integer attemptsAllowed) {
        this.actionId = actionId;
        this.action = action;
        this.otp = otp;
        this.receiverEmail = receiverEmail;
        this.userId = userId;
        this.attemptsAllowed = attemptsAllowed;
    }

    @PrePersist
    void onCreate(){
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate(){
        this.updatedAt = LocalDateTime.now();
    }
}
