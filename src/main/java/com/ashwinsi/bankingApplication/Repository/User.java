package com.ashwinsi.bankingApplication.Repository;

import com.ashwinsi.bankingApplication.DTO.RoleEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true, length = 120)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(length = 20)
  private String phoneNumber;

  @Column(length = 255)
  private String address;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private RoleEnum role = RoleEnum.USER;

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Account> accountList;

  @OneToMany(mappedBy = "sender", fetch = FetchType.LAZY)
  private List<Transaction> sentTransactions;

  @OneToMany(mappedBy = "receiver", fetch = FetchType.LAZY)
  private List<Transaction> receivedTransactions;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  void onCreate() {
    this.createdAt = LocalDateTime.now();
  }
}
