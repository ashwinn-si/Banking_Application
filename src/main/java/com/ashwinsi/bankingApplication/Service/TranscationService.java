package com.ashwinsi.bankingApplication.Service;

import com.ashwinsi.bankingApplication.DTO.CustomError;
import com.ashwinsi.bankingApplication.DTO.Enum.TransactionStatusEnum;
import com.ashwinsi.bankingApplication.DTO.Enum.TransactionTypeEnum;
import com.ashwinsi.bankingApplication.DTO.GetAllDTO;
import com.ashwinsi.bankingApplication.Domain.Account;
import com.ashwinsi.bankingApplication.Domain.Transaction;
import com.ashwinsi.bankingApplication.Domain.User;
import com.ashwinsi.bankingApplication.Repository.TransactionRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
class GetTransactionDTO {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class User {
        private UUID id;
        private UUID accountId;
        private String name;
        private String phoneNumber;
        private String email;
    }

    private UUID transactionId;
    private User sender;
    private User receiver;
    private Long amount;
    private String comments;
    private TransactionTypeEnum transactionType;
    private TransactionStatusEnum transactionStatus;
    private LocalDateTime createdAt;

    public GetTransactionDTO(UUID transactionId, Long amount, String comments,
            TransactionTypeEnum transactionType, TransactionStatusEnum transactionStatus,
            LocalDateTime createdAt) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.comments = comments;
        this.transactionType = transactionType;
        this.transactionStatus = transactionStatus;
        this.createdAt = createdAt;
        this.sender = null;
        this.receiver = null;
    }
}


@Data
@AllArgsConstructor
@NoArgsConstructor
class GetAllTransactionDTO {
    private UUID transactionId;
    private UUID senderAccountId;
    private UUID receiverAccountId;
    private TransactionTypeEnum transactionType;
    private TransactionStatusEnum transactionStatus;
    private Long amount;
    private LocalDateTime createdAt;
}


@Data
@AllArgsConstructor
@NoArgsConstructor
class TransactionStartDTO {
    private UUID transcationId;
}


@Service
public class TranscationService {
    private TransactionRepository transactionRepository;
    private AccountService accountService;

    TranscationService(TransactionRepository transactionRepository, UserService userService,
            AccountService accountService) {
        this.accountService = accountService;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public TransactionStartDTO startTransaction(UUID depositAccountId, TransactionTypeEnum transactionTypeEnum)
            throws Exception {
        Account senderAccount = accountService.findAccount(depositAccountId);

        if(senderAccount.isBlocked()){
            throw new CustomError("Account is blocked. Cannot start transaction", HttpStatus.BAD_REQUEST);
        }

        Transaction transaction = new Transaction();
        transaction.setTransactionType(transactionTypeEnum);
        transaction.startTranscation(senderAccount);

        Transaction savedTranscation = transactionRepository.save(transaction);

        return new TransactionStartDTO(savedTranscation.getId());
    }

    @Transactional
    public TransactionStartDTO startTransaction(UUID senderAccountId, UUID receiverAccountId, TransactionTypeEnum transactionTypeEnum)
            throws Exception {
        Account senderAccount = accountService.findAccount(senderAccountId);
        Account receiverAccount = accountService.findAccount(receiverAccountId);

        if(senderAccount.isBlocked()){
            throw new CustomError("Sender Account is blocked. Cannot start transaction", HttpStatus.BAD_REQUEST);
        }

        if(receiverAccount.isBlocked()){
            throw new CustomError("Receiver Account is blocked. Cannot start transaction", HttpStatus.BAD_REQUEST);
        }

        Transaction transaction = new Transaction();
        transaction.setTransactionType(transactionTypeEnum);
        transaction.startTranscation(senderAccount, receiverAccount);

        Transaction savedTransaction = transactionRepository.save(transaction);

        return new TransactionStartDTO(savedTransaction.getId());
    }

    private Transaction isValidTranscation(UUID transcationId, TransactionTypeEnum transactionType)
            throws Exception {
        Transaction transaction = findTransaction(transcationId);
        try{
            if(!transaction.getTransactionType().equals(transactionType)){
                transaction.setTransactionStatus(TransactionStatusEnum.FAILED);
                transaction.setComments("Transaction expired. Time limit of 5 minutes exceeded");
                throw new CustomError("Invalid TransactionType", HttpStatus.BAD_REQUEST);
            }

            if(LocalDateTime.now().isAfter(transaction.getCreatedAt().plusMinutes(5))){
                transaction.setTransactionStatus(TransactionStatusEnum.FAILED);
                transaction.setComments("Transaction expired. Time limit of 5 minutes exceeded");
                throw new CustomError("Transaction expired. Time limit of 5 minutes exceeded", HttpStatus.BAD_REQUEST);
            }
            if(transaction.getTransactionStatus() != TransactionStatusEnum.STARTED){
                throw new CustomError("Idempotency Rule is stopping the execution", HttpStatus.BAD_REQUEST);
            }

            return transaction;
        }catch(Exception e){
            transactionRepository.save(transaction);
            throw e;
        }

    }


    @Transactional
    public void updateTransactionStatus(UUID transactionId,
            TransactionStatusEnum transactionStatusEnum) throws Exception {
        Transaction transaction = findTransaction(transactionId);
        transaction.setTransactionStatus(transactionStatusEnum);
        transactionRepository.save(transaction);
    }

    @Transactional
    public void updateTransactionComment(UUID transactionId, String comment) throws Exception {
        Transaction transaction = findTransaction(transactionId);
        transaction.setComments(comment);
        transactionRepository.save(transaction);
    }

    public void deposit(Long depositAmount, UUID depositAccountId, UUID transactionId) throws Exception {
        isValidTranscation(transactionId, TransactionTypeEnum.DEPOSIT);

        updateTransactionStatus(transactionId, TransactionStatusEnum.TRANSCATION_STARTED);

        try {
            accountService.updateAccountBalance(depositAccountId, depositAmount);
            updateTransactionStatus(transactionId, TransactionStatusEnum.COMPLETED);
        } catch (Exception e) {
            updateTransactionComment(transactionId, "inconsistency issue");
            updateTransactionStatus(transactionId, TransactionStatusEnum.FAILED);
            throw e;
        }
    }

    public void withdraw(Long withdrawAmount, UUID withdrawAccountId, UUID transactionId) throws Exception {
        isValidTranscation(transactionId, TransactionTypeEnum.WITHDRAW);

        updateTransactionStatus(transactionId, TransactionStatusEnum.TRANSCATION_STARTED);
        try {
            accountService.updateAccountBalance(withdrawAccountId, withdrawAmount * -1);
            updateTransactionStatus(transactionId, TransactionStatusEnum.COMPLETED);
        } catch (Exception e) {
            updateTransactionComment(transactionId, e.getMessage());
            updateTransactionStatus(transactionId, TransactionStatusEnum.FAILED);
            throw e;
        }
    }

    public void transfer(Long transferAmount, UUID senderAccountId, UUID receiverAccountId, UUID transactionId) throws Exception{
        isValidTranscation(transactionId, TransactionTypeEnum.TRANSACTION);
        updateTransactionStatus(transactionId, TransactionStatusEnum.TRANSCATION_STARTED);

        Account senderAccount = accountService.findAccount(senderAccountId);

        if(senderAccount.getBalance() < transferAmount){
            throw new CustomError("Insufficient Balance", HttpStatus.BAD_REQUEST);
        }

        try {
            accountService.updateAccountBalance(senderAccountId, transferAmount * -1);
            accountService.updateAccountBalance(receiverAccountId, transferAmount);
            updateTransactionStatus(transactionId, TransactionStatusEnum.COMPLETED);
        }catch (Exception e){
            updateTransactionComment(transactionId, e.getMessage());
            updateTransactionStatus(transactionId, TransactionStatusEnum.FAILED);
            throw e;
        }

    }


    public GetTransactionDTO getTransaction(UUID transactionId) throws CustomError {
        Transaction transaction = findTransaction(transactionId);
        GetTransactionDTO getTranscationDTO =
                new GetTransactionDTO(transactionId, transaction.getAmount(),
                        transaction.getComments(), transaction.getTransactionType(),
                        transaction.getTransactionStatus(), transaction.getCreatedAt());

        Account senderAccount = transaction.getSenderAccount();
        User sender = senderAccount.getUser();
        GetTransactionDTO.User senderDTO =
                new GetTransactionDTO.User(sender.getId(), senderAccount.getId(), sender.getName(),
                        sender.getPhoneNumber(), sender.getEmail());
        getTranscationDTO.setSender(senderDTO);

        if (transaction.getTransactionType() == TransactionTypeEnum.TRANSACTION) {
            Account recieverAccount = transaction.getReceiverAccount();
            User receiver = recieverAccount.getUser();
            GetTransactionDTO.User receiverDTO =
                    new GetTransactionDTO.User(receiver.getId(), recieverAccount.getId(),
                            receiver.getName(), receiver.getPhoneNumber(), receiver.getEmail());
            getTranscationDTO.setReceiver(receiverDTO);
        }

        return getTranscationDTO;
    }

    public GetAllDTO getAllTranscation(UUID accountId, UUID userId, Integer page, Integer size)
            throws Exception {

        boolean isAccountMappedToUser = accountService.isAccountMappedUser(accountId, userId);
        if (!isAccountMappedToUser) {
            throw new CustomError("Unauthorized access to account", HttpStatus.UNAUTHORIZED);
        }
        Pageable pageRequest = PageRequest.of(page - 1, size);

        Page<Transaction> transactionsPage = transactionRepository
                .findBySenderAccount_IdOrReceiverAccount_IdOrderByCreatedAtDesc(accountId,
                        accountId, pageRequest);
        List<Transaction> transactionsList = transactionsPage.getContent();

        List<GetAllTransactionDTO> transactionDTOS = new ArrayList<>();
        for (Transaction transaction : transactionsList) {
            if (transaction.getTransactionType().equals(TransactionTypeEnum.TRANSACTION)) {
                System.out.println(transaction.getReceiverAccount().getId()+"-------");
                transactionDTOS.add(new GetAllTransactionDTO(transaction.getId(),
                        transaction.getSenderAccount().getId(),
                        transaction.getReceiverAccount().getId(), transaction.getTransactionType(),
                        transaction.getTransactionStatus(), transaction.getAmount(),
                        transaction.getCreatedAt()));
            } else {
                transactionDTOS.add(new GetAllTransactionDTO(transaction.getId(),
                        transaction.getSenderAccount().getId(), null,
                        transaction.getTransactionType(), transaction.getTransactionStatus(),
                        transaction.getAmount(), transaction.getCreatedAt()));
            }

        }

        return new GetAllDTO(transactionDTOS, page, size, transactionsPage.getTotalPages());
    }

    private Transaction findTransaction(UUID transactionId) throws CustomError {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new CustomError("Transaction Not Found", HttpStatus.NOT_FOUND));
    }

    private boolean isTransactionExists(UUID transactionId) {
        return transactionRepository.findById(transactionId).isPresent();
    }
}
