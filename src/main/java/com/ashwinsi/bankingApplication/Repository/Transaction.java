package com.ashwinsi.bankingApplication.Repository;

import com.ashwinsi.bankingApplication.DTO.TransactionStatusEnum;
import com.ashwinsi.bankingApplication.DTO.TransactionTypeEnum;
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
  @JoinColumn(name = "sender_id")
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private User sender;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receiver_id")
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private User receiver;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private TransactionTypeEnum transactionType;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal amount;

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
