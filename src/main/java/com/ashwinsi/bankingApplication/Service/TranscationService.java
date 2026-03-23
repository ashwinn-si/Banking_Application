package com.ashwinsi.bankingApplication.Service;

import com.ashwinsi.bankingApplication.DTO.CustomError;
import com.ashwinsi.bankingApplication.DTO.Enum.TransactionStatusEnum;
import com.ashwinsi.bankingApplication.DTO.Enum.TransactionTypeEnum;
import com.ashwinsi.bankingApplication.Domain.Account;
import com.ashwinsi.bankingApplication.Domain.Transaction;
import com.ashwinsi.bankingApplication.Domain.User;
import com.ashwinsi.bankingApplication.Repository.TransactionRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
class GetTranscationDTO {
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

  public GetTranscationDTO(UUID transactionId, Long amount, String comments,
      TransactionTypeEnum transactionType,
      TransactionStatusEnum transactionStatus, LocalDateTime createdAt) {
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
class DepositStartDTO {
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
  public DepositStartDTO startTransaction(UUID depositId, TransactionTypeEnum transactionTypeEnum) throws Exception {
    Account senderAccount = accountService.findAccount(depositId);

    Transaction transaction = new Transaction();
    transaction.setTransactionType(transactionTypeEnum);
    transaction.startDeposit(senderAccount);

    Transaction savedTranscation = transactionRepository.save(transaction);

    return new DepositStartDTO(savedTranscation.getId());
  }

  private boolean isTransactionStarted(UUID transcationId) throws Exception {
    Transaction transaction = findTranscation(transcationId);
    return transaction.getTransactionStatus() == TransactionStatusEnum.STARTED;
  }

  @Transactional
  public void updateTranscationStatus(UUID transactionId, TransactionStatusEnum transactionStatusEnum)
      throws Exception {
    Transaction transaction = findTranscation(transactionId);
    transaction.setTransactionStatus(transactionStatusEnum);
    transactionRepository.save(transaction);
  }

  @Transactional
  public void updateTransactionComment(UUID transactionId, String comment) throws Exception {
    Transaction transaction = findTranscation(transactionId);
    transaction.setComments(comment);
    transactionRepository.save(transaction);
  }

  public void deposit(Long depositAmount, UUID depositId, UUID transactionId) throws Exception {
    boolean hasStarted = isTransactionStarted(transactionId);
    if (!hasStarted) {
      throw new CustomError("Idempotency law stopping the execution", HttpStatus.BAD_REQUEST);
    }
    updateTranscationStatus(transactionId, TransactionStatusEnum.TRANSCATION_STARTED);
    try {
      accountService.updateAccountBalance(depositId, depositAmount);
      updateTranscationStatus(transactionId, TransactionStatusEnum.COMPLETED);
    } catch (Exception e) {
        updateTransactionComment(transactionId, "inconsistency issue");
      updateTranscationStatus(transactionId, TransactionStatusEnum.FAILED);
      throw e;
    }
  }

    public void withdraw(Long withdrawAmount, UUID depositId, UUID transactionId) throws Exception {
        boolean hasStarted = isTransactionStarted(transactionId);
        if (!hasStarted) {
            throw new CustomError("Idempotency law stopping the execution", HttpStatus.BAD_REQUEST);
        }
        updateTranscationStatus(transactionId, TransactionStatusEnum.TRANSCATION_STARTED);
        try {
            accountService.updateAccountBalance(depositId, withdrawAmount * -1);
            updateTranscationStatus(transactionId, TransactionStatusEnum.COMPLETED);
        } catch (Exception e) {
            updateTransactionComment(transactionId, e.getMessage());
            updateTranscationStatus(transactionId, TransactionStatusEnum.FAILED);
            throw e;
        }
    }

  public void withdraw( UUID depositId, UUID transactionId, Long withdrawAmount) throws Exception {
      boolean hasStarted = isTransactionStarted(transactionId);
      if (!hasStarted) {
          throw new CustomError("Idempotency law stopping the execution", HttpStatus.BAD_REQUEST);
      }
      updateTranscationStatus(transactionId, TransactionStatusEnum.TRANSCATION_STARTED);

  }

  public GetTranscationDTO getTransaction(UUID transactionId) throws CustomError {
    Transaction transaction = findTranscation(transactionId);
    GetTranscationDTO getTranscationDTO = new GetTranscationDTO(transactionId, transaction.getAmount(),
        transaction.getComments(),
        transaction.getTransactionType(), transaction.getTransactionStatus(), transaction.getCreatedAt());

    Account senderAccount = transaction.getSenderAccount();
    User sender = senderAccount.getUser();
    GetTranscationDTO.User senderDTO = new GetTranscationDTO.User(sender.getId(),
        senderAccount.getId(), sender.getName(), sender.getPhoneNumber(), sender.getEmail());
    getTranscationDTO.setSender(senderDTO);

    if (transaction.getTransactionType() == TransactionTypeEnum.TRANSACTION) {
      Account recieverAccount = transaction.getReceiverAccount();
      User receiver = recieverAccount.getUser();
      GetTranscationDTO.User receiverDTO = new GetTranscationDTO.User(receiver.getId(),
          recieverAccount.getId(), receiver.getName(), receiver.getPhoneNumber(), receiver.getEmail());
      getTranscationDTO.setReceiver(receiverDTO);
    }

    return getTranscationDTO;
  }

  // public List<TranscationDTO> getAllTranscation(UUID userId) throws Exception{
  // User user = userService.findUser(userId, null, null);
  //
  // List<Transaction> sendTranscation = user.getSentTransactions();
  // List<Transaction> recieverTranscation = user.getReceivedTransactions();
  //
  // List<TranscationDTO> transactionDTOS = new ArrayList<>();
  //
  // for(Transaction transaction: sendTranscation){
  //
  // }
  //
  // return transactionDTOS;
  // }

  private Transaction findTranscation(UUID transactionId) throws CustomError {
    return transactionRepository.findById(transactionId)
        .orElseThrow(() -> new CustomError("Transaction Not Found", HttpStatus.NOT_FOUND));
  }

  private boolean isTransactionExists(UUID transactionId) {
    return transactionRepository.findById(transactionId).isPresent();
  }
}
