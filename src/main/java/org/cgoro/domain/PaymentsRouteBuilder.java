package org.cgoro.domain;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.cgoro.db.dao.AccountDAO;
import org.cgoro.db.dao.LedgerDAO;
import org.cgoro.db.dao.ReceiptDAO;
import org.cgoro.db.dao.TransactionDAO;
import org.cgoro.db.entity.*;
import org.cgoro.exception.DoublePaymentException;
import org.cgoro.exception.DuplicateTransactionIdException;
import org.cgoro.exception.InSufficientFundsException;
import org.cgoro.exception.NotFoundException;
import org.cgoro.mappers.TransactionMapper;
import org.cgoro.model.PaymentOrderDTO;
import org.cgoro.model.ReceiptDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentsRouteBuilder  extends RouteBuilder {
    @Override
    public void configure() throws Exception {

        onException(DuplicateTransactionIdException.class).handled(true)
                .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("400")).setBody().constant("Transaction Id already exists");

        onException(DoublePaymentException.class).handled(true)
                .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("400")).setBody().constant("Double Payment not allowed");

        onException(NotFoundException.class).handled(true)
                .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("404")).setBody().constant("Not Found");

        onException(InSufficientFundsException.class).handled(true)
                .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("400")).setBody().constant("Insufficient Funds");

        from("direct:submitPayment")
                .setHeader("paymentOrder", simple("${body}"))
                .process(exchange -> {
                    PaymentOrderDTO paymentOrderDTO = exchange.getIn().getBody(PaymentOrderDTO.class);
                    TransactionDAO transactionDAO = (TransactionDAO) exchange.getContext().getRegistry().lookupByName("transactionDAO");
                    if (transactionDAO.exists(paymentOrderDTO.getTransactionId())) {
                        throw new DuplicateTransactionIdException();
                    }
                })
                .process(exchange -> {
                    PaymentOrderDTO paymentOrderDTO = exchange.getIn().getBody(PaymentOrderDTO.class);
                    TransactionDAO transactionDAO = (TransactionDAO) exchange.getContext().getRegistry().lookupByName("transactionDAO");
                    if (transactionDAO.findByApplicationRefIdAndStatus(paymentOrderDTO.getApplicationRefId(), TransactionStatus.SUCCESFULL) != null) {
                        throw new DoublePaymentException();
                    }
                })
                .process(exchange -> {
                    PaymentOrderDTO paymentOrderDTO = exchange.getIn().getBody(PaymentOrderDTO.class);
                    Transaction transaction = TransactionMapper.INSTANCE.paymentOrderToTransaction(paymentOrderDTO);
                    AccountDAO accountDAO = (AccountDAO) exchange.getContext().getRegistry().lookupByName("accountDAO");
                    TransactionDAO transactionDAO = (TransactionDAO) exchange.getContext().getRegistry().lookupByName("transactionDAO");
                    BalanceService balanceService = (BalanceService) exchange.getContext().getRegistry().lookupByName("balanceService");

                    Account recipient = accountDAO.find(paymentOrderDTO.getRecipientAccountId());
                    Account sender = accountDAO.find(paymentOrderDTO.getSenderAccountId());

                    transaction.setRecipient(recipient);
                    transaction.setSender(sender);
                    transaction.setPaymentOrderCreationDate(LocalDateTime.now());

                    balanceService.checkSenderBalance(transaction);

                    transaction.setStatus(TransactionStatus.INPROGRESS);

                    Receipt receipt = new Receipt();
                    receipt.setReceiptToken(UUID.randomUUID().toString());
                    receipt.setTransaction(transaction);
                    paymentOrderDTO.setReceiptToken(receipt.getReceiptToken());
                    exchange.getIn().setBody(paymentOrderDTO);
                    transaction.setReceipt(receipt);

                    transactionDAO.save(transaction);
                });


        from("direct:finalizePayment")
                .process(exchange -> {
                    String transactionId = exchange.getIn().getHeader("transactionId", String.class);
                    String receiptToken = exchange.getIn().getHeader("receiptToken", String.class);

                    ReceiptDAO receiptDAO = (ReceiptDAO) exchange.getContext().getRegistry().lookupByName("receiptDAO");
                    TransactionDAO transactionDAO = (TransactionDAO) exchange.getContext().getRegistry().lookupByName("transactionDAO");
                    LedgerDAO ledgerDao = (LedgerDAO) exchange.getContext().getRegistry().lookupByName("ledgerDAO");
                    Receipt receipt = receiptDAO.findByTransactionIdAndReceiptToken(transactionId,receiptToken);

                    Transaction transaction = receipt.getTransaction();



                    if(transaction.getStatus().equals(TransactionStatus.SUCCESFULL)) {
                        transaction.setPaymentReceiptCreationDate(LocalDateTime.now());
                        ledgerDao.findByTransactionId(transaction.getId()).forEach(ledgerUpdate -> {
                            ledgerUpdate.setStatus(LedgerUpdateStatus.FINAL);
                            ledgerDao.update(ledgerUpdate);
                        });
                    }

                    transactionDAO.update(transaction);
                    exchange.getIn().setBody(transaction);
                })
                .process(exchange -> {
                    Transaction transaction = exchange.getIn().getBody(Transaction.class);
                    ReceiptDTO receiptDTO = TransactionMapper.INSTANCE.transactionToReceiptDTO(transaction);
                    exchange.getIn().setBody(receiptDTO);
                });

    }

}
