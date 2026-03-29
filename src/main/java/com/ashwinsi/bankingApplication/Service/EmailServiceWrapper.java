package com.ashwinsi.bankingApplication.Service;

import com.ashwinsi.bankingApplication.DTO.CustomError;
import com.ashwinsi.bankingApplication.DTO.EmailEventDTO;
import com.ashwinsi.bankingApplication.DTO.Enum.EmailType;
import com.ashwinsi.bankingApplication.Kafka.EmailProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailServiceWrapper {
    private final AuditLogService auditLogService;
    private final EmailProducer emailProducer;

    public void sendEmail(String toEmail, UUID userId, String action, Integer otp)
            throws Exception {
        boolean isAllowed = auditLogService.isAllowedToPerform(userId, action);

        EmailEventDTO emailEvent =
                new EmailEventDTO(toEmail, otp, action, userId, EmailType.OTP_EMAIL);

        if (isAllowed) {
            emailProducer.sendOtpEmail(emailEvent);
        } else {
            throw new CustomError("Email Attempts Exceeded. Try After Sometime",
                    HttpStatus.BAD_REQUEST);
        }
    }

    public void sendEmail(String toEmail, UUID userId, String action) throws Exception {
        boolean isAllowed = auditLogService.isAllowedToPerform(userId, action);

        EmailEventDTO emailEvent =
                new EmailEventDTO(toEmail, null, action, userId, EmailType.INFORMATION_EMAIL);

        if (isAllowed) {
            emailProducer.sendOtpEmail(emailEvent);
        } else {
            throw new CustomError("Email Attempts Exceeded. Try After Sometime",
                    HttpStatus.BAD_REQUEST);
        }
    }
}
