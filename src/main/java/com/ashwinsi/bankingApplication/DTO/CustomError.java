package com.ashwinsi.bankingApplication.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
public class CustomError extends  Exception{
    private String message;
    private HttpStatus status;

    CustomError(String message, HttpStatus status){
        super(message);
        this.status = status;
        this.message = message;
    }
}
