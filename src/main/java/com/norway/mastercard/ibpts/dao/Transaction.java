package com.norway.mastercard.ibpts.dao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * The is a Transaction Entity class which is used to perform transaction specific sql operations.
 */
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer       transactionId;
    private Integer       accountId;
    private BigDecimal    amount;
    private String        type;
    private LocalDateTime transactionDate;
    @ManyToOne(targetEntity = Account.class)
    @JoinColumn(name = "parentAccountId")
    private Account       account;

}
