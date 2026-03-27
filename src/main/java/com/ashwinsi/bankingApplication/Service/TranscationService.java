package com.ashwinsi.bankingApplication.Service;

import com.ashwinsi.bankingApplication.DTO.CustomError;
import com.ashwinsi.bankingApplication.DTO.Enum.TransactionStatusEnum;
import com.ashwinsi.bankingApplication.DTO.Enum.TransactionTypeEnum;
import com.ashwinsi.bankingApplication.DTO.GetAllDTO;
import com.ashwinsi.bankingApplication.Domain.Account;
import com.ashwinsi.bankingApplication.Domain.Otp;
import com.ashwinsi.bankingApplication.Domain.Transaction;
import com.ashwinsi.bankingApplication.Domain.User;
import com.ashwinsi.bankingApplication.Repository.TransactionRepository;
import com.ashwinsi.bankingApplication.Utils.Auditable;
import com.ashwinsi.bankingApplication.Utils.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private UUID actionId;
}


@Service
public class TranscationService {
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final Map<UUID, Transaction> transactionCache = new HashMap<>();
    private final Map<UUID, UUID> accountIdUserIdCache = new HashMap<>();
    private final Map<UUID, LocalDateTime> transactionLastAccessTime = new HashMap<>();
    private final Map<UUID, LocalDateTime> accountIdUserIdLastAccessTime = new HashMap<>();

    private final AuditLogService auditLogService;
    private final OtpService otpService;
    private final EmailServiceWrapper emailService;

    TranscationService(TransactionRepository transactionRepository, UserService userService,
            AccountService accountService, AuditLogService auditLogService, OtpService otpService,
            EmailServiceWrapper emailService) {
        this.accountService = accountService;
        this.transactionRepository = transactionRepository;
        this.auditLogService = auditLogService;
        this.otpService = otpService;
        this.emailService = emailService;
    }

    private UUID createOtpForTransactionAction(Account senderAccount, String action)
            throws Exception {
        UUID actionId = UUID.randomUUID();
        Otp otp = otpService.createOtp(actionId, senderAccount.getUser().getEmail(),
                senderAccount.getUser().getId(), action);
        emailService.sendEmail(senderAccount.getUser().getEmail(), senderAccount.getUser().getId(),
                action, otp.getOtp());
        return otp.getActionId();
    }

    private void validateOtpAndBlockAccountOnExceeded(UUID accountId, UUID actionId,
            Integer enteredOtp, String action) throws Exception {
        UUID userId = getUserIdFromCache(accountId);
        Otp otp = otpService.findOtp(actionId);

        if (!action.equals(otp.getAction())) {
            throw new CustomError("Invalid OTP action", HttpStatus.BAD_REQUEST);
        }

        if (!userId.equals(otp.getUserId())) {
            throw new CustomError("Unauthorized OTP for this account", HttpStatus.UNAUTHORIZED);
        }

        try {
            boolean isCorrect = otpService.checkOtp(actionId, enteredOtp);
            if (isCorrect) {
                return;
            }

            Otp latestOtp = otpService.findOtp(actionId);
            if (latestOtp.getIncorrectAttempts() >= latestOtp.getAttemptsAllowed()) {
                accountService.updateAccountBlockStatus(accountId, true);
                throw new CustomError("Incorrect OTP attempts exceeded. Account blocked",
                        HttpStatus.BAD_REQUEST);
            }

            throw new CustomError("Incorrect OTP", HttpStatus.BAD_REQUEST);
        } catch (CustomError e) {
            if ("OTP Attempts Exceeded".equals(e.getMessage())) {
                accountService.updateAccountBlockStatus(accountId, true);
                throw new CustomError("OTP attempts exceeded. Account blocked",
                        HttpStatus.BAD_REQUEST);
            }
            throw e;
        }
    }

    private UUID getUserIdFromCache(Account account) {
        UUID accountId = account.getId();

        if (!accountIdUserIdCache.containsKey(accountId)) {
            accountIdUserIdCache.put(accountId, account.getUser().getId());
        }

        accountIdUserIdLastAccessTime.put(accountId, LocalDateTime.now());
        return accountIdUserIdCache.get(accountId);
    }

    private UUID getUserIdFromCache(UUID accountId) throws Exception {

        if (!accountIdUserIdCache.containsKey(accountId)) {
            Account account = accountService.findAccount(accountId);
            accountIdUserIdCache.put(accountId, account.getUser().getId());
        }

        accountIdUserIdLastAccessTime.put(accountId, LocalDateTime.now());
        return accountIdUserIdCache.get(accountId);
    }

    private Transaction getCachedTransaction(UUID transactionId) throws CustomError {
        if (transactionCache.containsKey(transactionId)) {
            transactionLastAccessTime.put(transactionId, LocalDateTime.now());
            return transactionCache.get(transactionId);
        }
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new CustomError("Transaction Not Found", HttpStatus.NOT_FOUND));
        transactionCache.put(transactionId, transaction);
        transactionLastAccessTime.put(transactionId, LocalDateTime.now());
        return transaction;
    }

    private void updateTransactionCache(Transaction transaction) {
        transactionCache.put(transaction.getId(), transaction);
        transactionLastAccessTime.put(transaction.getId(), LocalDateTime.now());
    }

    @Transactional
    @Auditable(action = Constants.ACTION_START_TRANSACTION)
    public TransactionStartDTO startTransaction(UUID depositAccountId,
            TransactionTypeEnum transactionTypeEnum) throws Exception {
        Account senderAccount = accountService.findAccount(depositAccountId);

        if (senderAccount.isBlocked()) {
            throw new CustomError("Account is blocked. Cannot start transaction",
                    HttpStatus.BAD_REQUEST);
        }

        UUID userId = getUserIdFromCache(senderAccount);

        if (!auditLogService.isAllowedToPerform(userId, Constants.ACTION_START_TRANSACTION)) {
            throw new CustomError("Limit to start transaction has Exceed. Try After Sometime",
                    HttpStatus.BAD_REQUEST);
        }

        Transaction transaction = new Transaction();
        transaction.setTransactionType(transactionTypeEnum);
        transaction.startTranscation(senderAccount);

        Transaction savedTranscation = transactionRepository.save(transaction);
        UUID actionId = null;
        if (TransactionTypeEnum.WITHDRAW.equals(transactionTypeEnum)) {
            actionId = createOtpForTransactionAction(senderAccount, Constants.ACTION_WITHDRAW);
        }

        return new TransactionStartDTO(savedTranscation.getId(), actionId);
    }

    @Transactional
    @Auditable(action = Constants.ACTION_START_TRANSACTION)
    public TransactionStartDTO startTransaction(UUID senderAccountId, UUID receiverAccountId,
            TransactionTypeEnum transactionTypeEnum) throws Exception {
        Account senderAccount = accountService.findAccount(senderAccountId);
        Account receiverAccount = accountService.findAccount(receiverAccountId);

        if (senderAccount.isBlocked()) {
            throw new CustomError("Sender Account is blocked. Cannot start transaction",
                    HttpStatus.BAD_REQUEST);
        }

        if (receiverAccount.isBlocked()) {
            throw new CustomError("Receiver Account is blocked. Cannot start transaction",
                    HttpStatus.BAD_REQUEST);
        }

        UUID userId = getUserIdFromCache(senderAccount);

        if (!auditLogService.isAllowedToPerform(userId, Constants.ACTION_START_TRANSACTION)) {
            throw new CustomError("Limit to start transaction has Exceed. Try After Sometime",
                    HttpStatus.BAD_REQUEST);
        }

        Transaction transaction = new Transaction();
        transaction.setTransactionType(transactionTypeEnum);
        transaction.startTranscation(senderAccount, receiverAccount);

        Transaction savedTransaction = transactionRepository.save(transaction);
        UUID actionId = createOtpForTransactionAction(senderAccount, Constants.ACTION_TRANSFER);

        return new TransactionStartDTO(savedTransaction.getId(), actionId);
    }

    private Transaction isValidTranscation(UUID transcationId, TransactionTypeEnum transactionType)
            throws Exception {
        Transaction transaction = findTransaction(transcationId);
        try {
            if (!transaction.getTransactionType().equals(transactionType)) {
                transaction.setTransactionStatus(TransactionStatusEnum.FAILED);
                transaction.setComments("Invalid Transaction Type Attempt");
                throw new CustomError("Invalid TransactionType", HttpStatus.BAD_REQUEST);
            }

            if (LocalDateTime.now().isAfter(transaction.getCreatedAt().plusMinutes(5))) {
                transaction.setTransactionStatus(TransactionStatusEnum.FAILED);
                transaction.setComments("Transaction expired. Time limit of 5 minutes exceeded");
                throw new CustomError("Transaction expired. Time limit of 5 minutes exceeded",
                        HttpStatus.BAD_REQUEST);
            }
            if (transaction.getTransactionStatus() != TransactionStatusEnum.INITIATED) {
                throw new CustomError("Idempotency Rule is stopping the execution",
                        HttpStatus.BAD_REQUEST);
            }

            return transaction;
        } catch (Exception e) {
            transactionRepository.save(transaction);
            throw e;
        }

    }


    @Transactional
    public void updateTransactionStatus(UUID transactionId,
            TransactionStatusEnum transactionStatusEnum) throws Exception {
        Transaction transaction = getCachedTransaction(transactionId);
        transaction.setTransactionStatus(transactionStatusEnum);
        transactionRepository.save(transaction);
        updateTransactionCache(transaction);
    }

    @Transactional
    public void updateTransactionComment(UUID transactionId, String comment) throws Exception {
        Transaction transaction = getCachedTransaction(transactionId);
        transaction.setComments(comment);
        transactionRepository.save(transaction);
        updateTransactionCache(transaction);
    }

    @Transactional
    public void updateTransactionAmount(UUID transactionId, Long amount) throws Exception {
        Transaction transaction = getCachedTransaction(transactionId);
        transaction.setAmount(amount);
        transactionRepository.save(transaction);
        updateTransactionCache(transaction);
    }

    @Auditable(action = Constants.ACTION_DEPOSIT)
    public void deposit(Long depositAmount, UUID depositAccountId, UUID transactionId)
            throws Exception {
        isValidTranscation(transactionId, TransactionTypeEnum.DEPOSIT);
        updateTransactionStatus(transactionId, TransactionStatusEnum.TRANSACTION_STARTED);

        try {
            accountService.updateAccountBalance(depositAccountId, depositAmount);
            updateTransactionAmount(transactionId, depositAmount);
            updateTransactionStatus(transactionId, TransactionStatusEnum.COMPLETED);
        } catch (Exception e) {
            updateTransactionComment(transactionId, "inconsistency issue");
            updateTransactionStatus(transactionId, TransactionStatusEnum.FAILED);
            throw e;
        }
    }

    @Auditable(action = Constants.ACTION_WITHDRAW)
    public void withdraw(Long withdrawAmount, UUID withdrawAccountId, UUID transactionId,
            UUID actionId, Integer otp) throws Exception {
        isValidTranscation(transactionId, TransactionTypeEnum.WITHDRAW);

        validateOtpAndBlockAccountOnExceeded(withdrawAccountId, actionId, otp,
                Constants.ACTION_WITHDRAW);

        UUID userId = getUserIdFromCache(withdrawAccountId);

        if (!auditLogService.isAllowedToPerform(userId, Constants.ACTION_WITHDRAW)) {
            // IMP BLOCKING THE ACCOUNT AS TOO-MUCH ATTEMPTS ARE MADE
            accountService.updateAccountBlockStatus(withdrawAccountId, true);

            throw new CustomError("Limit to start transaction has Exceed. Try After Sometime",
                    HttpStatus.BAD_REQUEST);
        }

        updateTransactionStatus(transactionId, TransactionStatusEnum.TRANSACTION_STARTED);

        try {
            accountService.updateAccountBalance(withdrawAccountId, withdrawAmount * -1);
            updateTransactionAmount(transactionId, withdrawAmount);
            updateTransactionStatus(transactionId, TransactionStatusEnum.COMPLETED);
        } catch (Exception e) {
            updateTransactionComment(transactionId, e.getMessage());
            updateTransactionStatus(transactionId, TransactionStatusEnum.FAILED);
            throw e;
        }
    }

    @Auditable(action = Constants.ACTION_TRANSFER)
    public void transfer(Long transferAmount, UUID senderAccountId, UUID receiverAccountId,
            UUID transactionId, UUID actionId, Integer otp) throws Exception {
        isValidTranscation(transactionId, TransactionTypeEnum.TRANSACTION);

        validateOtpAndBlockAccountOnExceeded(senderAccountId, actionId, otp,
                Constants.ACTION_TRANSFER);

        Account senderAccount = accountService.findAccount(senderAccountId);

        if (senderAccount.getBalance() < transferAmount) {
            throw new CustomError("Insufficient Balance", HttpStatus.BAD_REQUEST);
        }

        UUID userId = getUserIdFromCache(senderAccountId);

        if (!auditLogService.isAllowedToPerform(userId, Constants.ACTION_TRANSFER)) {
            // IMP BLOCKING THE ACCOUNT AS TOO-MUCH ATTEMPTS ARE MADE
            accountService.updateAccountBlockStatus(senderAccountId, true);

            throw new CustomError("Limit to start transaction has Exceed. Try After Sometime",
                    HttpStatus.BAD_REQUEST);
        }

        updateTransactionStatus(transactionId, TransactionStatusEnum.TRANSACTION_STARTED);

        try {
            accountService.updateAccountBalance(senderAccountId, transferAmount * -1);
            accountService.updateAccountBalance(receiverAccountId, transferAmount);
            updateTransactionAmount(transactionId, transferAmount);
            updateTransactionStatus(transactionId, TransactionStatusEnum.COMPLETED);
        } catch (Exception e) {
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

    public GetAllDTO<List<GetAllTransactionDTO>> getAllTranscation(UUID accountId, UUID userId,
            Integer page, Integer size) throws Exception {

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
                System.out.println(transaction.getReceiverAccount().getId() + "-------");
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

        return new GetAllDTO<>(transactionDTOS, page, size, transactionsPage.getTotalPages());
    }

    private Transaction findTransaction(UUID transactionId) throws CustomError {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new CustomError("Transaction Not Found", HttpStatus.NOT_FOUND));
    }

    private boolean isTransactionExists(UUID transactionId) {
        return transactionRepository.findById(transactionId).isPresent();
    }

    // Runs every 10 minutes to clean up stale elements in cache
    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void evictStaleCache() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(10);

        transactionLastAccessTime.entrySet().removeIf(entry -> {
            LocalDateTime accessedAt = entry.getValue();
            if (accessedAt != null && accessedAt.isBefore(cutoff)) {
                transactionCache.remove(entry.getKey());
                return true;
            }
            return false;
        });

        accountIdUserIdLastAccessTime.entrySet().removeIf(entry -> {
            LocalDateTime accessedAt = entry.getValue();
            if (accessedAt != null && accessedAt.isBefore(cutoff)) {
                accountIdUserIdCache.remove(entry.getKey());
                return true;
            }
            return false;
        });

        System.out.println("[TransactionService] Cache eviction ran. Remaining transactions: "
                + transactionCache.size() + ", Remaining accountIds: "
                + accountIdUserIdCache.size());
    }

}
