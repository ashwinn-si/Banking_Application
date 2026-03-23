package com.ashwinsi.bankingApplication.Repository;

import com.ashwinsi.bankingApplication.Domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
}
