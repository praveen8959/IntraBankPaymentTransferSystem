package com.norway.mastercard.ibpts.service.internal;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.norway.mastercard.ibpts.model.AccountDetails;
import com.norway.mastercard.ibpts.model.TransactionType;
import com.norway.mastercard.ibpts.service.AccountService;

/**
 * This class is the mock implementation class for AccountService interface which returs mock data when profile is activated.
 */
@Component
@Profile("mockService")
public class AccountServiceMockImpl implements AccountService {

    public static final int    ACCOUNT_ID      = 111;
    public static final String CURRENCY        = "NOK";
    public static final int    FROM_ACCOUNT_ID = 222;

    /**
     * Generates mock data.
     *
     * @param accountId accountId to read.
     * @return AccountDetails details of the account.
     */
    @Override
    public AccountDetails getAccountBalance(int accountId) {
        if (accountId == ACCOUNT_ID) {
            return AccountDetails.builder()
                    .accountId(ACCOUNT_ID)
                    .balance(TEN)
                    .currencyCode(CURRENCY)
                    .build();
        }
        return AccountDetails.builder().build();
    }

    /**
     * Generates mock data.
     *
     * @param accountId accountId to read.
     * @return List<AccountDetails> details of the account transaction.
     */
    @Override
    public List<AccountDetails> getMiniStatement(int accountId) {
        if (accountId == ACCOUNT_ID) {
            return List.of(transactionDetails(ONE), transactionDetails(TEN));
        }
        return Collections.emptyList();
    }

    private AccountDetails transactionDetails(BigDecimal amount) {
        return AccountDetails.builder()
                .accountId(FROM_ACCOUNT_ID)
                .amount(amount)
                .currencyCode(CURRENCY)
                .transactionDate(LocalDateTime.now(ZoneId.systemDefault()))
                .type(TransactionType.DEBIT)
                .build();
    }

    /**
     * When called will just do nothing.
     *
     * @param toAccountId accountId to read.
     * @param accountDetails fromAccount details to read.
     */
    @Override
    public void transferAmount(int toAccountId, AccountDetails accountDetails) {
        // mock implementation no need to perform operation for void.
    }
}
