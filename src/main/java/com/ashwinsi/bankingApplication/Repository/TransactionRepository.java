package com.ashwinsi.bankingApplication.Repository;

import com.ashwinsi.bankingApplication.Domain.Transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Page<Transaction> findBySenderAccount_IdOrReceiverAccount_IdOrderByCreatedAtDesc(
            UUID senderAccountId, UUID receiverAccountId, Pageable pageable);
}
