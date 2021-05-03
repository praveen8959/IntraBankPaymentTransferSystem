package com.norway.mastercard.ibpts.service;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import com.norway.mastercard.ibpts.dao.Account;
import com.norway.mastercard.ibpts.dao.Transaction;
import com.norway.mastercard.ibpts.model.AccountDetails;
import com.norway.mastercard.ibpts.model.TransactionType;
import com.norway.mastercard.ibpts.repo.AccountRepository;
import com.norway.mastercard.ibpts.service.internal.AccountServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.norway.mastercard.ibpts.exception.AccountNotFoundException;

import lombok.val;

/**
 * The is test class for AccountServiceImpl and uses mockito mocks for the account repository.
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    public static final int    ACCOUNT_ID      = 111;
    public static final String CURRENCY        = "NOK";
    public static final int    FROM_ACCOUNT_ID = 222;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    void getAccountBalance_whenValidAccountId() {
        when(accountRepository.findById(ACCOUNT_ID))
                .thenReturn(Optional.of(Account.builder()
                        .accountId(ACCOUNT_ID)
                        .balance(TEN)
                        .currency(CURRENCY)
                        .build()));
        val accountBalance = accountService.getAccountBalance(ACCOUNT_ID);
        Assertions.assertThat(accountBalance).isNotNull();
        Assertions.assertThat(accountBalance.getAccountId()).isEqualTo(ACCOUNT_ID);
        Assertions.assertThat(accountBalance.getBalance()).isEqualTo(TEN);
        Assertions.assertThat(accountBalance.getCurrencyCode()).isEqualTo(CURRENCY);
    }

    @Test
    void getAccountBalance_whenInvalidAccountId_throwException() {
        when(accountRepository.findById(ACCOUNT_ID))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> accountService.getAccountBalance(ACCOUNT_ID))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("Invalid Account ID " + ACCOUNT_ID);
    }

    @Test
    void getMiniStatement_whenValidAccountId() {
        val transactionOne = prepareTransactionsMock(TransactionType.DEBIT.name(), TEN);
        val transactionTwo = prepareTransactionsMock(TransactionType.CREDIT.name(), ONE);
        when(accountRepository.findById(ACCOUNT_ID))
                .thenReturn(Optional.of(Account.builder()
                        .accountId(ACCOUNT_ID)
                        .currency(CURRENCY)
                        .transactions(List.of(transactionOne, transactionTwo))
                        .build()));
        List<AccountDetails> accountDetails = accountService.getMiniStatement(ACCOUNT_ID);
        assertThat(accountDetails)
                .flatExtracting("accountId", "amount", "currencyCode", "type")
                .contains(ACCOUNT_ID, TEN, CURRENCY, TransactionType.DEBIT)
                .contains(ACCOUNT_ID, ONE, CURRENCY, TransactionType.CREDIT);

    }

    @Test
    void getMiniStatement_whenValidAccountId_throwException() {
        when(accountRepository.findById(ACCOUNT_ID))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> accountService.getMiniStatement(ACCOUNT_ID))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("Invalid Account ID " + ACCOUNT_ID);
    }

    @Test
    void transferAmount_whenValidAccountId() {
        val transactionOne = prepareTransactionsMock(TransactionType.DEBIT.name(), TEN);
        val transactionTwo = prepareTransactionsMock(TransactionType.CREDIT.name(), ONE);
        mockToAccountDetails(transactionOne, transactionTwo);
        mockFromAccountDetails(transactionOne);
        accountService.transferAmount(ACCOUNT_ID, AccountDetails.builder()
                .accountId(FROM_ACCOUNT_ID)
                .currencyCode(CURRENCY)
                .balance(TEN)
                .amount(ONE)
                .type(TransactionType.DEBIT)
                .build());

        verify(accountRepository, times(2)).findById(anyInt());
        verify(accountRepository, times(2)).save(any(Account.class));

    }

    private void mockFromAccountDetails(Transaction transactionOne) {
        when(accountRepository.findById(FROM_ACCOUNT_ID))
                .thenReturn(Optional.of(Account.builder()
                        .accountId(FROM_ACCOUNT_ID)
                        .currency(CURRENCY)
                        .balance(TEN)
                        .transactions(List.of(transactionOne))
                        .build()));
    }

    @Test
    void transferAmount_whenAmountIsGreaterThanBalance() {
        val transactionOne = prepareTransactionsMock(TransactionType.DEBIT.name(), TEN);
        val transactionTwo = prepareTransactionsMock(TransactionType.CREDIT.name(), ONE);
        mockToAccountDetails(transactionOne, transactionTwo);
        mockFromAccountDetails(transactionOne);
        AccountDetails fromAccountRequest = AccountDetails.builder()
                .accountId(FROM_ACCOUNT_ID)
                .currencyCode(CURRENCY)
                .balance(TEN)
                .amount(BigDecimal.valueOf(20))
                .type(TransactionType.DEBIT)
                .build();
        assertThatThrownBy(() -> accountService.transferAmount(ACCOUNT_ID, fromAccountRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient funds available");
        verify(accountRepository, times(2)).findById(anyInt());

    }

    private void mockToAccountDetails(Transaction transactionOne, Transaction transactionTwo) {
        when(accountRepository.findById(ACCOUNT_ID))
                .thenReturn(Optional.of(Account.builder()
                        .accountId(ACCOUNT_ID)
                        .currency(CURRENCY)
                        .balance(TEN)
                        .transactions(List.of(transactionOne, transactionTwo))
                        .build()));
    }

    @Test
    void transferAmount_whenValidToAccountId_throwException() {
        when(accountRepository.findById(ACCOUNT_ID))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> accountService.transferAmount(ACCOUNT_ID, any()))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("Invalid Account ID " + ACCOUNT_ID);
    }

    @Test
    void transferAmount_whenValidFromAccountId_throwException() {
        when(accountRepository.findById(anyInt()))
                .thenReturn(Optional.of(Account.builder()
                        .accountId(ACCOUNT_ID)
                        .balance(TEN)
                        .currency(CURRENCY)
                        .build()), Optional.empty());
        assertThatThrownBy(() -> accountService.transferAmount(ACCOUNT_ID, AccountDetails.builder().accountId(FROM_ACCOUNT_ID).build()))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("Invalid Account ID " + FROM_ACCOUNT_ID);
    }

    private Transaction prepareTransactionsMock(String type, BigDecimal amount) {
        return Transaction.builder()
                .type(type)
                .amount(amount)
                .transactionDate(LocalDateTime.now(ZoneId.systemDefault()))
                .build();
    }

}
