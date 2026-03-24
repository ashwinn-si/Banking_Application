package com.ashwinsi.bankingApplication.Service;

import com.ashwinsi.bankingApplication.DTO.AccountDTO;
import com.ashwinsi.bankingApplication.DTO.CustomError;
import com.ashwinsi.bankingApplication.Domain.Account;
import com.ashwinsi.bankingApplication.Domain.User;
import com.ashwinsi.bankingApplication.Repository.AccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccountService {
  private AccountRepository accountRepository;
  private UserService userService;

  AccountService(AccountRepository accountRepository, UserService userService) {
    this.accountRepository = accountRepository;
    this.userService = userService;
  }

  public List<AccountDTO> getAllAccount(UUID userId) throws Exception {
    Boolean isUserExists = userService.isUserExists(userId, null, null);
    if (!isUserExists) {
      throw new CustomError("USER NOT FOUND", HttpStatus.NOT_FOUND);
    }

    List<Account> accounts = accountRepository.findAllByUserId(userId);

    List<AccountDTO> accountDTOs = new ArrayList<>();
    for (Account account : accounts) {
      accountDTOs.add(new AccountDTO(account.getId(), account.getBalance(), account.isBlocked()));
    }

    return accountDTOs;
  }

  public AccountDTO getAccount(UUID accountId, UUID userId) throws Exception {
    Account account = findAccount(accountId);
    User user = account.getUser();

    if (!user.getId().equals(userId)) {
      throw new CustomError("ACCOUNT IS NOT MAPPED TO THE USER", HttpStatus.UNAUTHORIZED);
    }
    return new AccountDTO(account.getId(), account.getBalance(), account.isBlocked());
  }

  @Transactional
  public void createAccount(UUID userId) throws Exception {
    User user = userService.findUser(userId, null, null);
    List<AccountDTO> allAccounts = getAllAccount(user.getId());

    long balance = 0L;
    if (allAccounts.size() == 0) {
      // INITIAL BONUS
      balance = 10000;
    }

    Account account = new Account();
    account.setBalance(balance);
    account.setUser(user);

    accountRepository.save(account);
  }

  public boolean isAccountExists(UUID accountId) {
    Optional<Account> account = accountRepository.findById(accountId);
    return account.isPresent();
  }

  public Account findAccount(UUID accountId) throws Exception {
    System.out.println("asdasdsadsadsadsadsadasd=====" + accountId);
    Account account = accountRepository.findById(accountId)
        .orElseThrow(() -> new CustomError("Account Not Found", HttpStatus.NOT_FOUND));
    return account;
  }

  public boolean isAccountMappedUser(UUID accountId, UUID userId) {
    return accountRepository.findByIdAndUser_Id(accountId, userId).isPresent();
  }

  @Transactional
  public boolean updateAccountBalance(UUID accountId, Long amount) throws Exception {
    if (amount == null) {
      throw new CustomError("AMOUNT IS REQUIRED", HttpStatus.BAD_REQUEST);
    }

    Account account = findAccount(accountId);
    long updatedBalance = account.getBalance() + amount;

    if (updatedBalance < 0) {
      throw new CustomError("INSUFFICIENT BALANCE", HttpStatus.BAD_REQUEST);
    }

    account.setBalance(updatedBalance);
    accountRepository.save(account);
    return true;
  }

}
