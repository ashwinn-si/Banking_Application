package com.ashwinsi.bankingApplication.Domain;

import com.ashwinsi.bankingApplication.DTO.Enum.RoleEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true, length = 120)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(length = 20)
  private String phoneNumber;

  @Column(length = 255)
  private String address;

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Account> accountList = new ArrayList<>();

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  private boolean isActivated = true;

  public User(String name, String email, String hashPassword, String phoneNumber, String address) {
    this.name = name;
    this.email = email;
    this.password = hashPassword;
    this.phoneNumber = phoneNumber;
    this.address = address;
  }

  @PrePersist
  void onCreate() {
    this.createdAt = LocalDateTime.now();
  }
}
