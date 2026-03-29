package com.ashwinsi.bankingApplication.DTO;

import com.ashwinsi.bankingApplication.DTO.Enum.EmailType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailEventDTO {
    private String toEmail;
    private Integer otp;
    private String action;
    private UUID userId;
    private EmailType emailType;
}
