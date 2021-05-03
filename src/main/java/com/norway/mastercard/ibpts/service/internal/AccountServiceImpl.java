package com.norway.mastercard.ibpts.service.internal;

import static com.norway.mastercard.ibpts.model.TransactionType.CREDIT;
import static com.norway.mastercard.ibpts.model.TransactionType.DEBIT;
import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.norway.mastercard.ibpts.dao.Account;
import com.norway.mastercard.ibpts.dao.Transaction;
import com.norway.mastercard.ibpts.exception.AccountNotFoundException;
import com.norway.mastercard.ibpts.model.AccountDetails;
import com.norway.mastercard.ibpts.model.TransactionType;
import com.norway.mastercard.ibpts.repo.AccountRepository;
import com.norway.mastercard.ibpts.service.AccountService;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * The AccountServiceImpl is a implementation of AccountService interface.
 */
@Component
@RequiredArgsConstructor
@Profile("!mockService")
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    /**
     * Reads account balance.
     *
     * @param accountId accountId to read.
     * @return AccountDetails details of the account.
     */
    @Override
    public AccountDetails getAccountBalance(int accountId) {
        val account = getAccountById(accountId);
        return AccountDetails.builder()
                .balance(account.getBalance())
                .accountId(account.getAccountId())
                .currencyCode(account.getCurrency())
                .build();
    }

    /**
     * Reads account and transactions.
     * 
     * @param accountId accountId to read.
     * @return List<AccountDetails> details of the account transaction.
     */
    @Override
    public List<AccountDetails> getMiniStatement(int accountId) {
        val account = getAccountById(accountId);
        return mapRepositoryToService(account);
    }

    /**
     * Reads account balance of toAccountId and fromAccountId validates and performs transfer of amount from one account to another.
     *
     * @param toAccountId accountId to read.
     * @param accountDetails fromAccount details to read.
     */
    @Override
    public void transferAmount(int toAccountId, AccountDetails accountDetails) {
        val toAccount = getAccountById(toAccountId);
        val fromAccount = getAccountById(accountDetails.getAccountId());
        validateFunds(accountDetails, fromAccount, toAccount.getBalance());
        if (isGreater(accountDetails.getAmount(), ZERO)) {
            performTransactionOfAmount(accountDetails, toAccount, fromAccount);
        }
    }

    private void performTransactionOfAmount(AccountDetails accountDetails, Account toAccount, Account fromAccount) {
        toAccount.setBalance(setAccountBalance(accountDetails, toAccount.getBalance(), true));
        List<Transaction> transactions = new ArrayList<>(toAccount.getTransactions());
        transactions.add(prepareNewTransaction(accountDetails, toAccount));
        toAccount.setTransactions(transactions);
        accountRepository.save(toAccount);
        fromAccount.setBalance(setAccountBalance(accountDetails, fromAccount.getBalance(), false));
        accountRepository.save(fromAccount);
    }

    private void validateFunds(AccountDetails accountDetails, Account fromAccount, BigDecimal balance) {
        if (CREDIT.equals(accountDetails.getType()) && !isGreater(fromAccount.getBalance(), accountDetails.getAmount())
                || DEBIT.equals(accountDetails.getType()) && !isGreater(balance, accountDetails.getAmount()))
            throw new IllegalArgumentException("Insufficient funds available");
    }

    private List<AccountDetails> mapRepositoryToService(Account account) {
        return account.getTransactions().stream().map(transaction -> mapTransaction(transaction, account)).collect(Collectors.toList());
    }

    private AccountDetails mapTransaction(Transaction transaction, Account account) {
        return AccountDetails.builder()
                .accountId(account.getAccountId())
                .currencyCode(account.getCurrency())
                .transactionDate(transaction.getTransactionDate())
                .amount(transaction.getAmount())
                .type(TransactionType.valueOf(transaction.getType()))
                .build();
    }

    private Account getAccountById(int accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Invalid Account ID " + accountId));
    }

    private Transaction prepareNewTransaction(AccountDetails accountDetails, Account account) {
        return Transaction.builder()
                .amount(accountDetails.getAmount())
                .accountId(accountDetails.getAccountId())
                .transactionDate(accountDetails.getTransactionDate())
                .type(accountDetails.getType().name())
                .account(account)
                .build();
    }

    private BigDecimal setAccountBalance(AccountDetails accountDetails, BigDecimal balance, boolean isToAccount) {
        if (TransactionType.DEBIT.equals(accountDetails.getType()))
            return isToAccount ? balance.subtract(accountDetails.getAmount()) : balance.add(accountDetails.getAmount());
        else if (CREDIT.equals(accountDetails.getType()))
            return isToAccount ? balance.add(accountDetails.getAmount()) : balance.subtract(accountDetails.getAmount());
        else
            throw new IllegalArgumentException("Not a valid transaction type");
    }

    private static <T extends Comparable<T>> boolean isGreater(T t1, T t2) {
        return t1.compareTo(t2) > 0;
    }

}
