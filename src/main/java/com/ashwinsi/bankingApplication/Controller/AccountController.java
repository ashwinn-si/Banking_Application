package com.ashwinsi.bankingApplication.Controller;

import com.ashwinsi.bankingApplication.DTO.JwtDTO;
import com.ashwinsi.bankingApplication.Service.AccountService;
import com.ashwinsi.bankingApplication.Utils.ResponseHandler;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private AccountService accountService;

    AccountController(AccountService accountService){
        this.accountService = accountService;
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllAccount(@AuthenticationPrincipal JwtDTO jwtDTO) throws Exception {
        return ResponseHandler.handleResponse(HttpStatus.OK, accountService.getAllAccount(jwtDTO.getUserId()), "All account details");
    }

    @GetMapping("/get/{accountId}")
    public ResponseEntity<?> getAccount(@AuthenticationPrincipal JwtDTO jwtDTO, @PathVariable UUID accountId) throws Exception {
        return ResponseHandler.handleResponse(HttpStatus.OK, accountService.getAccount(accountId, jwtDTO.getUserId()), "Account details");
    }

    @PostMapping("/add")
    public ResponseEntity<?> addAccount(@AuthenticationPrincipal JwtDTO jwtDTO) throws Exception {
        accountService.createAccount(jwtDTO.getUserId());
        return ResponseHandler.handleResponse(HttpStatus.OK, null, "Account Created");
    }

}
