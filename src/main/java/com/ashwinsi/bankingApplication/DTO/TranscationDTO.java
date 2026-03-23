package com.ashwinsi.bankingApplication.DTO;

import java.time.LocalDateTime;
import java.util.UUID;
import com.ashwinsi.bankingApplication.DTO.Enum.TransactionStatusEnum;
import com.ashwinsi.bankingApplication.DTO.Enum.TransactionTypeEnum;

public class TranscationDTO {
    private UUID transactionId;
    private UUID senderId;
    private UUID recieverId;
    private Long amount;
    private String comments;
    private TransactionTypeEnum transactionType;
    private TransactionStatusEnum transactionStatus;
    private LocalDateTime createdAt;
}
