package com.norway.mastercard.ibpts.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.norway.mastercard.ibpts.model.AccountDetails;
import com.norway.mastercard.ibpts.service.AccountService;

import lombok.RequiredArgsConstructor;

/**
 * The is Account resource class which acts as a controller to perform account specific operations.
 */
@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountResource {

    private final AccountService accountService;

    @PostMapping("/{accountId}")
    public ResponseEntity<Void> transferAmount(@PathVariable("accountId") int toAccountId, @RequestBody AccountDetails accountDetails) {
        accountService.transferAmount(toAccountId, accountDetails);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{accountId}/balance")
    public AccountDetails getAccountBalance(@PathVariable int accountId) {
        return accountService.getAccountBalance(accountId);
    }

    @GetMapping("/{accountId}/statements/mini")
    public List<AccountDetails> getMiniStatement(@PathVariable int accountId) {
        return accountService.getMiniStatement(accountId);
    }

}
