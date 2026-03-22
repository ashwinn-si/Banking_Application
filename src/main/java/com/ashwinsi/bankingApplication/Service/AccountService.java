package com.ashwinsi.bankingApplication.Service;

import com.ashwinsi.bankingApplication.DTO.AccountDTO;
import com.ashwinsi.bankingApplication.DTO.CustomError;
import com.ashwinsi.bankingApplication.DTO.UserDTO;
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

    AccountService(AccountRepository accountRepository, UserService userService){
        this.accountRepository = accountRepository;
        this.userService = userService;
    }

    List<AccountDTO> getAllAccount(UUID userId) throws Exception {
        Boolean isUserExists = userService.isUserExists(userId, null, null);
        if(!isUserExists){
            throw new CustomError("USER NOT FOUND", HttpStatus.NOT_FOUND);
        }

        List<Account> accounts = accountRepository.findAllByUserId(userId);

        List<AccountDTO> accountDTOs = new ArrayList<>();
        for(Account account: accounts){
            accountDTOs.add(new AccountDTO(account.getId(), account.getBalance(), account.isBlocked()));
        }

        return accountDTOs;
    }

    AccountDTO getAccount(UUID accountId) throws Exception{
        Account account = findAccount(accountId);
        return new AccountDTO(account.getId(), account.getBalance(), account.isBlocked());
    }

    @Transactional
    void createAccount(UUID userId) throws Exception {
        User user = userService.findUser(userId, null, null);
        List<AccountDTO> allAccounts = getAllAccount(user.getId());

        long balance = 0L;
        if(allAccounts.size() == 0){
            // INITIAL BONUS
            balance = 10000;
        }

        Account account = new Account();
        account.setBalance(balance);
        account.setUser(user);

        accountRepository.save(account);
    }


    boolean isAccountExists(UUID accountId){
        Optional<Account> account = accountRepository.findById(accountId);
        return account.isPresent();
    }

    Account findAccount(UUID accountId) throws Exception{
        Account account = accountRepository.findById(accountId).orElseThrow(() ->
                new CustomError("Account Not Found", HttpStatus.NOT_FOUND)
        );
        return account;
    }


}
