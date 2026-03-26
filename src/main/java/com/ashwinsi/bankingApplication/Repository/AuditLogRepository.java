package com.ashwinsi.bankingApplication.Repository;

import com.ashwinsi.bankingApplication.Domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

}
