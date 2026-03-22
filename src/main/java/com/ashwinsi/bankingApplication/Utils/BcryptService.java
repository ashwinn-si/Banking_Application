package com.ashwinsi.bankingApplication.Utils;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BcryptService {
    PasswordEncoder passwordEncoder;

    BcryptService(PasswordEncoder passwordEncoder){
        this.passwordEncoder = passwordEncoder;
    }

    String generateEncodedPassword(String password){
        return passwordEncoder.encode(password);
    }

    Boolean checkEncodedPassword(String encodedPassword, String rawPassword){
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
