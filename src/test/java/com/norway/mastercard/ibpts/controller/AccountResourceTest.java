package com.norway.mastercard.ibpts.controller;

import static com.norway.mastercard.ibpts.model.TransactionType.DEBIT;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.norway.mastercard.ibpts.model.AccountDetails;
import com.norway.mastercard.ibpts.service.AccountService;

import lombok.val;

/**
 * The is test class for AccountResource and uses mockito mocks for the service calls.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { AccountResource.class })
@WebMvcTest
class AccountResourceTest {

    public static final int    ACCOUNT_ID      = 111;
    public static final String CURRENCY        = "NOK";
    public static final int    FROM_ACCOUNT_ID = 222;
    @Autowired
    MockMvc                    mockMvc;

    @MockBean
    AccountService             accountService;

    @Test
    void getAccountBalanceTest() throws Exception {
        when(accountService.getAccountBalance(ACCOUNT_ID)).thenReturn(AccountDetails.builder()
                .accountId(ACCOUNT_ID)
                .balance(BigDecimal.TEN)
                .currencyCode(CURRENCY)
                .build());
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .get("/accounts/{accountId}/balance", ACCOUNT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        ObjectMapper objectMapper = new ObjectMapper();
        val accountDetails = objectMapper.readValue(result.getResponse().getContentAsString(), AccountDetails.class);
        assertThat(accountDetails).isNotNull();
        assertThat(accountDetails.getAccountId()).isEqualTo(ACCOUNT_ID);
        assertThat(accountDetails.getCurrencyCode()).isEqualTo(CURRENCY);
        assertThat(accountDetails.getBalance()).isEqualTo(BigDecimal.TEN);
        verify(accountService).getAccountBalance(ACCOUNT_ID);
    }

    @Test
    void getMiniStatement() throws Exception {
        when(accountService.getMiniStatement(anyInt())).thenReturn(List.of(transactionDetails(ONE), transactionDetails(TEN)));
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .get("/accounts/{accountId}/statements/mini", ACCOUNT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        List<AccountDetails> accountDetails = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
        });
        assertThat(accountDetails).isNotNull();
        assertThat(accountDetails)
                .flatExtracting("accountId", "currencyCode", "type")
                .contains(FROM_ACCOUNT_ID, CURRENCY, DEBIT)
                .contains(FROM_ACCOUNT_ID, CURRENCY, DEBIT);
    }

    private AccountDetails transactionDetails(BigDecimal amount) {
        return AccountDetails.builder()
                .accountId(FROM_ACCOUNT_ID)
                .amount(amount)
                .currencyCode(CURRENCY)
                .transactionDate(LocalDateTime.now(ZoneId.systemDefault()))
                .type(DEBIT)
                .build();
    }

    @Test
    void transferAmount() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper
                .writeValueAsString(AccountDetails.builder()
                        .amount(BigDecimal.TEN)
                        .type(DEBIT)
                        .accountId(ACCOUNT_ID)
                        .build());
        mockMvc.perform(MockMvcRequestBuilders
                .post("/accounts/{accountId}", FROM_ACCOUNT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
