package com.ashwinsi.bankingApplication.Utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
class ResponseData <T>{
    private T data;
    private String message;
    private Boolean success;
}

@Component
public class ResponseHandler {

    public static <T> ResponseEntity<?> handleResponse(HttpStatus status, T data, String message, T success){

        return ResponseEntity.status(status).body(new ResponseData(data, message, success == null ? true : (Boolean) success));
    }
}
