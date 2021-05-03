package com.norway.mastercard.ibpts;

import static com.norway.mastercard.ibpts.model.TransactionType.CREDIT;
import static com.norway.mastercard.ibpts.model.TransactionType.DEBIT;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.norway.mastercard.ibpts.model.AccountDetails;

import lombok.val;

/**
 * The is integration test class for AccountResource.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = IntraBankPaymentTransferSystem.class)
@AutoConfigureMockMvc
class IntraBankPaymentTransferSystemServiceImplTest {

    public static final int    ACCOUNT_ID = 111;
    public static final String CURRENCY   = "NOK";
    @Autowired
    MockMvc                    mockMvc;

    @Test
    void getAccountBalance() throws Exception {
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
        assertThat(accountDetails.getBalance().longValue()).isEqualTo(500L);
    }

    @Test
    void getMiniStatement() throws Exception {
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
                .contains(ACCOUNT_ID, CURRENCY, DEBIT)
                .contains(ACCOUNT_ID, CURRENCY, CREDIT);
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
                .post("/accounts/{accountId}", 222)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonString))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

}
