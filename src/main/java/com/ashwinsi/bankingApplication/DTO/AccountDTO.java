package com.ashwinsi.bankingApplication.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountDTO {
    private UUID accountId;
    private Long balance;
    private Boolean isBlocked;
}
