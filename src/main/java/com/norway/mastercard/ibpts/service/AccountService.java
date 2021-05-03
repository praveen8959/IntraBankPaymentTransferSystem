package com.norway.mastercard.ibpts.service;

import java.util.List;

import com.norway.mastercard.ibpts.model.AccountDetails;

public interface AccountService {

    AccountDetails getAccountBalance(int accountId);

    List<AccountDetails> getMiniStatement(int accountId);

    void transferAmount(int toAccountId, AccountDetails accountDetails);

}
