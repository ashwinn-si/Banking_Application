package com.ashwinsi.bankingApplication.Service;

import com.ashwinsi.bankingApplication.DTO.CustomError;
import com.ashwinsi.bankingApplication.DTO.EmailContentDTO;
import com.ashwinsi.bankingApplication.Utils.EmailContextService;
import com.ashwinsi.bankingApplication.Utils.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EmailServiceWrapper {
    private final EmailService emailService;
    private final AuditLogService auditLogService;
    private final EmailContextService emailContextService;

    EmailServiceWrapper(EmailService emailService, AuditLogService auditLogService,
            EmailContextService emailContextService) {
        this.emailService = emailService;
        this.auditLogService = auditLogService;
        this.emailContextService = emailContextService;
    }

    public void sendEmail(String receiverEmail, UUID userId, String action, Integer otp)
            throws Exception {
        boolean isAllowed = auditLogService.isAllowedToPerform(userId, action);

        EmailContentDTO emailContentDTO = emailContextService.getEmailContent(action, otp);

        if (isAllowed) {
            emailService.sendEmail(receiverEmail, emailContentDTO.getSubject(),
                    emailContentDTO.getBody(), action);
        } else {
            throw new CustomError("Email Attempts Exceeded. Try After Sometime",
                    HttpStatus.BAD_REQUEST);
        }
    }

    public void sendEmail(String receiverEmail, UUID userId, String action) throws Exception {
        boolean isAllowed = auditLogService.isAllowedToPerform(userId, action);

        EmailContentDTO emailContentDTO = emailContextService.getEmailContent(action);

        if (isAllowed) {
            emailService.sendEmail(receiverEmail, emailContentDTO.getSubject(),
                    emailContentDTO.getBody(), action);
        } else {
            throw new CustomError("Email Attempts Exceeded. Try After Sometime",
                    HttpStatus.BAD_REQUEST);
        }
    }
}
