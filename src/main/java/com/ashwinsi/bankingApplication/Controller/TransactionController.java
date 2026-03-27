package com.ashwinsi.bankingApplication.Controller;

import com.ashwinsi.bankingApplication.DTO.Enum.TransactionTypeEnum;
import com.ashwinsi.bankingApplication.DTO.JwtDTO;
import com.ashwinsi.bankingApplication.Service.TranscationService;
import com.ashwinsi.bankingApplication.Utils.ResponseHandler;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
class TransactionStartDTO {
    private UUID accountId;
    private UUID receiverAccountId;
}


@Data
@AllArgsConstructor
@NoArgsConstructor
class TransactionDTO {
    private UUID senderAccountId;
    private Long amount;
    private UUID transactionId;
    private UUID receiverAccountId;
    private UUID actionId;
    private Integer otp;
}


@RestController
@RequestMapping("/api/transaction")
@Valid
public class TransactionController {
    private final TranscationService transcationService;

    TransactionController(TranscationService transcationService) {
        this.transcationService = transcationService;
    }

    @GetMapping("/get/{transactionId}")
    ResponseEntity<?> get(@PathVariable UUID transactionId) throws Exception {
        return ResponseHandler.handleResponse(HttpStatus.OK,
                transcationService.getTransaction(transactionId), "Transaction Details");
    }

    @GetMapping("/get-all/{accountId}")
    ResponseEntity<?> getAll(@PathVariable UUID accountId, @AuthenticationPrincipal JwtDTO jwtDTO,
            @RequestParam Integer page, @RequestParam Integer size) throws Exception {
        return ResponseHandler.handleResponse(HttpStatus.OK,
                transcationService.getAllTranscation(accountId, jwtDTO.getUserId(), page, size),
                "Transaction Details");
    }

    @PostMapping("/start-withdraw")
    ResponseEntity<?> withdrawStart(@RequestBody @Validated TransactionStartDTO transactionDTO)
            throws Exception {
        return ResponseHandler.handleResponse(HttpStatus.OK, transcationService
                .startTransaction(transactionDTO.getAccountId(), TransactionTypeEnum.WITHDRAW),
                "Transaction Started");
    }

    @PostMapping("/withdraw")
    ResponseEntity<?> withdraw(@RequestBody @Validated TransactionDTO transactionDTO)
            throws Exception {
        transcationService.withdraw(transactionDTO.getAmount(), transactionDTO.getSenderAccountId(),
                transactionDTO.getTransactionId(), transactionDTO.getActionId(),
                transactionDTO.getOtp());
        return ResponseHandler.handleResponse(HttpStatus.OK, null, "Withdraw Sucessfull");
    }

    @PostMapping("/admin/start-deposit")
    ResponseEntity<?> depositStart(@RequestBody @Validated TransactionStartDTO transactionDTO)
            throws Exception {
        return ResponseHandler.handleResponse(HttpStatus.OK, transcationService.startTransaction(
                transactionDTO.getAccountId(), TransactionTypeEnum.DEPOSIT), "Transaction Started");
    }

    @PostMapping("/admin/deposit")
    ResponseEntity<?> deposit(@RequestBody @Validated TransactionDTO depositDTO) throws Exception {
        transcationService.deposit(depositDTO.getAmount(), depositDTO.getSenderAccountId(),
                depositDTO.getTransactionId());
        return ResponseHandler.handleResponse(HttpStatus.OK, null, "Amount Successfully Deposited");
    }

    @PostMapping("/start-transfer")
    ResponseEntity<?> transferStart(@RequestBody @Validated TransactionStartDTO transactionDTO)
            throws Exception {
        return ResponseHandler.handleResponse(HttpStatus.OK,
                transcationService.startTransaction(transactionDTO.getAccountId(),
                        transactionDTO.getReceiverAccountId(), TransactionTypeEnum.TRANSACTION),
                "Transaction Started");
    }

    @PostMapping("/transfer")
    ResponseEntity<?> transfer(@RequestBody @Validated TransactionDTO transactionDTO)
            throws Exception {
        transcationService.transfer(transactionDTO.getAmount(), transactionDTO.getSenderAccountId(),
                transactionDTO.getReceiverAccountId(), transactionDTO.getTransactionId(),
                transactionDTO.getActionId(), transactionDTO.getOtp());
        return ResponseHandler.handleResponse(HttpStatus.OK, null, "Transfer Successful");
    }
}
