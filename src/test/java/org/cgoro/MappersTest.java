package org.cgoro;

import org.cgoro.db.entity.Account;
import org.cgoro.db.entity.Transaction;
import org.cgoro.db.entity.TransactionStatus;
import org.cgoro.mappers.AccountMapper;
import org.cgoro.mappers.TransactionMapper;
import org.cgoro.model.AccountDTO;
import org.cgoro.model.PaymentOrderDTO;
import org.cgoro.model.ReceiptDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(JUnit4.class)
public class MappersTest {

    @Test
    public void shouldMapAccountToDto() {
        //given
        Account account = new Account();
        account.setNameEn("test");
        account.setAccountId("x");
        account.setCreationDate(LocalDateTime.now());

        //when
        AccountDTO accountDTO = AccountMapper.INSTANCE.accountToDTO(account);

        //then
        assertNotNull(accountDTO);
        assertEquals(account.getAccountId(),accountDTO.getAccountId());
        assertEquals(account.getNameEn(),accountDTO.getNameEn());
        assertEquals(account.getCreationDate(), accountDTO.getCreationDate());
    }

    @Test
    public void shouldMapPaymentOrdertToTransaction() {
        //given
        PaymentOrderDTO paymentOrderDTO = new PaymentOrderDTO();
        paymentOrderDTO.setTransactionId("TXN"+ LocalDateTime.now().toString());
        paymentOrderDTO.setApplicationRefId("APPPAYMENT"+Math.random());
        paymentOrderDTO.setAmount(BigDecimal.valueOf(5000));

        //when
        Transaction transaction = TransactionMapper.INSTANCE.paymentOrderToTransaction(paymentOrderDTO);

        //then
        assertNotNull(paymentOrderDTO);
        assertEquals(paymentOrderDTO.getTransactionId(),transaction.getTransactionId());
        assertEquals(paymentOrderDTO.getApplicationRefId(),transaction.getApplicationRefId());
        assertEquals(paymentOrderDTO.getAmount(), transaction.getAmount());
    }

    @Test
    public void shouldMaptransactionToReceiptDTO() {
        //given
        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setApplicationRefId(UUID.randomUUID().toString());
        transaction.setPaymentId(UUID.randomUUID().toString());
        transaction.setStatus(TransactionStatus.SUCCESFULL);
        transaction.setAmount(BigDecimal.valueOf(5000));

        //when
        ReceiptDTO receiptDTO = TransactionMapper.INSTANCE.transactionToReceiptDTO(transaction);

        //then
        assertNotNull(receiptDTO);
        assertEquals(receiptDTO.getTransactionId(),transaction.getTransactionId());
        assertEquals(receiptDTO.getApplicationRefId(),transaction.getApplicationRefId());
        assertEquals(receiptDTO.getAmount(), transaction.getAmount());
        assertEquals(receiptDTO.getPaymentId(), transaction.getPaymentId());
        assertEquals(receiptDTO.getStatus(), transaction.getStatus());


    }

}


