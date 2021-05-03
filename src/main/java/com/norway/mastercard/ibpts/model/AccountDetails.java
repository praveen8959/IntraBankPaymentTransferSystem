package com.norway.mastercard.ibpts.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * The AccountDetails is a model which is reused in account controller by multiple operations, and used in service class.
 * Holds the account information and transaction details.
 */
@Builder
@Getter
@JsonInclude(value = Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class AccountDetails {

    private int             accountId;
    private BigDecimal      balance;
    private BigDecimal      amount;
    private String          currencyCode;
    private TransactionType type;
    private LocalDateTime   transactionDate;

}
