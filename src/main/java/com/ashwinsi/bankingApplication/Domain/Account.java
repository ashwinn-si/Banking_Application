package com.ashwinsi.bankingApplication.Domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, precision = 19, scale = 2)
  private Long balance = 0L;

  @Column(nullable = false)
  private boolean isBlocked = false;

  @Version
  private long version;


  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id")
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private User user;

  @OneToMany(mappedBy = "senderAccount", fetch = FetchType.LAZY)
  private List<Transaction> sentTransactions = new ArrayList<>();

  @OneToMany(mappedBy = "receiverAccount", fetch = FetchType.LAZY)
  private List<Transaction> receivedTransactions = new ArrayList<>();

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  @PrePersist
  void onCreate() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  void onUpdate(){
    this.updatedAt = LocalDateTime.now();
  }
}
