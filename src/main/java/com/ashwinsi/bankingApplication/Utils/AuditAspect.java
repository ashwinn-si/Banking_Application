package com.ashwinsi.bankingApplication.Utils;

import com.ashwinsi.bankingApplication.Domain.AuditLog;
import com.ashwinsi.bankingApplication.DTO.JwtDTO;
import com.ashwinsi.bankingApplication.Repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private static final Logger logger = LoggerFactory.getLogger(AuditAspect.class);

    private final AuditLogRepository auditRepo;

    @Around("@annotation(auditable)")
    public Object logAudit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {

        Object result;

        UUID id = null;
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if (securityContext.getAuthentication() != null && 
            securityContext.getAuthentication().getPrincipal() != null) {
            Object principal = securityContext.getAuthentication().getPrincipal();
            if (principal instanceof JwtDTO) {
                id = ((JwtDTO) principal).getUserId();
                logger.info("Extracted userId from JwtDTO: {}", id);
            } else {
                logger.warn("Unexpected principal type: {}", principal.getClass().getName());
            }
        }

        String methodName = joinPoint.getSignature().getName();

        logger.info("AuditAspect triggered for method: {} with action: {}", methodName, auditable.action());

        result = joinPoint.proceed();

        // Extract endpoint from HttpServletRequest
        String endpoint = null;
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            endpoint = request.getRequestURI();
        }

        AuditLog log = new AuditLog();
        log.setUserId(id);
        log.setAction(auditable.action());
        log.setMethod(methodName);
        log.setTimestamp(LocalDateTime.now());
        log.setDetails(Arrays.toString(joinPoint.getArgs()));
        log.setEndpoint(endpoint);

        try {
            auditRepo.save(log);
            logger.info("Audit log saved successfully: {}", log);
        } catch (Exception e) {
            logger.error("Failed to save audit log: {}", e.getMessage(), e);
        }

        return result;
    }
}
