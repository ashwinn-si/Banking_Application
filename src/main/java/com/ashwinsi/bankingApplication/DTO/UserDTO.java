package com.ashwinsi.bankingApplication.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private UUID id;
    private String email;
    private String phoneNumber;
    private String password;
    private Boolean isActivated;
}
