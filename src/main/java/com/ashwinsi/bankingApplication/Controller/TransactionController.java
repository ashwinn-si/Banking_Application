package com.ashwinsi.bankingApplication.Controller;

import com.ashwinsi.bankingApplication.DTO.Enum.TransactionTypeEnum;
import com.ashwinsi.bankingApplication.Service.TranscationService;
import com.ashwinsi.bankingApplication.Utils.ResponseHandler;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
class DepositStartDTO{
    private UUID accountId;
}


@Data
@AllArgsConstructor
@NoArgsConstructor
class TranscationDTO{
    private UUID senderId;
    private Long amount;
    private UUID transactionId;
    private UUID receiverId;
}

@RestController
@RequestMapping("/api/transaction")
@Valid
public class TransactionController {
    private TranscationService transcationService;

    TransactionController(TranscationService transcationService){
        this.transcationService = transcationService;
    }

    @GetMapping("/get/{transactionId}")
    ResponseEntity<?> get(@PathVariable  UUID transactionId) throws Exception {
        return ResponseHandler.handleResponse(HttpStatus.OK, transcationService.getTransaction(transactionId),
                "Transaction Details");
    }

    @PostMapping("/start-withdraw")
    ResponseEntity<?> withdrawStart(@RequestBody @Validated DepositStartDTO depositDTO) throws Exception {
        return ResponseHandler.handleResponse(HttpStatus.OK,
                transcationService.startTransaction(depositDTO.getAccountId(), TransactionTypeEnum.WITHDRAW), "Transaction Started");
    }

    @PostMapping("/withdraw")
    ResponseEntity<?> withdraw(@RequestBody @Validated TranscationDTO depositDTO) throws Exception {
        transcationService.withdraw(depositDTO.getAmount(), depositDTO.getSenderId(), depositDTO.getTransactionId());
        return ResponseHandler.handleResponse(HttpStatus.OK, null, "Withdraw Sucessfull");
    }

    @PostMapping("/start-deposit")
    ResponseEntity<?> depositStart(@RequestBody @Validated DepositStartDTO depositDTO) throws Exception {
        return ResponseHandler.handleResponse(HttpStatus.OK,
                transcationService.startTransaction(depositDTO.getAccountId(), TransactionTypeEnum.DEPOSIT), "Transaction Started");
    }

    // TODO ONLY ADMIN CAN DEPOSIT
    @PostMapping("/deposit")
    ResponseEntity<?> deposit(@RequestBody @Validated TranscationDTO depositDTO) throws  Exception{
        transcationService.deposit(depositDTO.getAmount(), depositDTO.getSenderId(), depositDTO.getTransactionId());
        return ResponseHandler.handleResponse(HttpStatus.OK, null, "Amount Successfully Deposited");
    }
}
