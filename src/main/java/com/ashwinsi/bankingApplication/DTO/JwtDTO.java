package com.ashwinsi.bankingApplication.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtDTO {
    private long userId;
    private String role; // "ADMIN" | "USER"
}
