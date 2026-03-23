package com.ashwinsi.bankingApplication.Domain;

import com.ashwinsi.bankingApplication.DTO.Enum.TransactionStatusEnum;
import com.ashwinsi.bankingApplication.DTO.Enum.TransactionTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "sender_account_id")
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private Account senderAccount;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receiver_account_id")
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private Account receiverAccount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private TransactionTypeEnum transactionType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private TransactionStatusEnum transactionStatus;

  @Column(nullable = false)
  private Long amount;

  @Column(nullable = false, length = 500)
  private String comments = "";

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private TransactionStatusEnum status = TransactionStatusEnum.STARTED;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  void onCreate() {
    this.createdAt = LocalDateTime.now();
  }
}
