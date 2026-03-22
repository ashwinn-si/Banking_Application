package com.ashwinsi.bankingApplication.Config;

import com.ashwinsi.bankingApplication.DTO.CustomError;
import com.ashwinsi.bankingApplication.Utils.ResponseHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class GlobalErrorHandler {

    // this type of error occurs when springboot tries to convert a string to int when coming in request params, query
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException exception){
        return ResponseHandler.handleResponse(HttpStatus.BAD_REQUEST, null, exception.getMessage(), false);
    }

    // this type of error occurs in springboot validation class like not null, min, max, etc
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception){
        return ResponseHandler.handleResponse(HttpStatus.BAD_REQUEST, null, exception.getMessage(), false);
    }

    @ExceptionHandler(CustomError.class)
    public ResponseEntity<?> handleCustomException(CustomError customError){
        return ResponseHandler.handleResponse(customError.getStatus(), null, customError.getMessage(), false);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleCommonException(Exception exception){
        return ResponseHandler.handleResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, exception.getMessage(), false);
    }
}
