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

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
class GetTranscationDTO{
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class User{
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

@Service
public class TranscationService {
    private TransactionRepository transactionRepository;

    TranscationService(TransactionRepository transactionRepository){
        this.transactionRepository = transactionRepository;
    }

    public GetTranscationDTO getTransaction(UUID transactionId, UUID senderId, UUID receiverId) throws CustomError {
        Transaction transaction = findTranscation(transactionId);
        GetTranscationDTO getTranscationDTO = new GetTranscationDTO(transactionId, transaction.getAmount(), transaction.getComments(),
                transaction.getTransactionType(), transaction.getTransactionStatus(), transaction.getCreatedAt());

        Account senderAccount = transaction.getSenderAccount();
        User sender = senderAccount.getUser();
        GetTranscationDTO.User senderDTO = new GetTranscationDTO.User(sender.getId(),
                senderAccount.getId(), sender.getName(), sender.getPhoneNumber(), sender.getEmail());
        getTranscationDTO.setSender(senderDTO);

        if(transaction.getTransactionType() == TransactionTypeEnum.TRANSACTION){
            Account recieverAccount = transaction.getReceiverAccount();
            User receiver = recieverAccount.getUser();
            GetTranscationDTO.User receiverDTO = new GetTranscationDTO.User(receiver.getId(),
                    recieverAccount.getId(), receiver.getName(), receiver.getPhoneNumber(), receiver.getEmail());
            getTranscationDTO.setReceiver(receiverDTO);
        }

        return getTranscationDTO;
    }

//    public List<TranscationDTO> getAllTranscation(UUID userId) throws  Exception{
//        User user = userService.findUser(userId, null, null);
//
//        List<Transaction> sendTranscation = user.getSentTransactions();
//        List<Transaction> recieverTranscation = user.getReceivedTransactions();
//
//        List<TranscationDTO> transactionDTOS = new ArrayList<>();
//
//        for(Transaction transaction: sendTranscation){
//
//        }
//
//        return transactionDTOS;
//    }

    private Transaction findTranscation(UUID transactionId) throws CustomError {
        return transactionRepository.findById(transactionId).orElseThrow( ()->
                new CustomError("Transaction Not Found", HttpStatus.NOT_FOUND));
    }

    private boolean isTransactionExists(UUID transactionId) {
        return transactionRepository.findById(transactionId).isPresent();
    }
}
