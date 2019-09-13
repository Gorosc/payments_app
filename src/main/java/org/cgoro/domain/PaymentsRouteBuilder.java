package org.cgoro.domain;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.cgoro.db.dao.AccountDAO;
import org.cgoro.db.dao.LedgerDAO;
import org.cgoro.db.dao.ReceiptDAO;
import org.cgoro.db.dao.TransactionDAO;
import org.cgoro.db.entity.*;
import org.cgoro.exception.*;
import org.cgoro.mappers.TransactionMapper;
import org.cgoro.model.PaymentOrderDTO;
import org.cgoro.model.ReceiptDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.NoResultException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PaymentsRouteBuilder  extends RouteBuilder {
    @Override
    public void configure() throws Exception {

        Logger logger = LoggerFactory.getLogger(this.getClass());

        onException(DuplicateTransactionIdException.class).handled(true).log(LoggingLevel.ERROR, "Transaction Id already exists")
                .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("400")).setBody().constant("Transaction Id already exists");

        onException(DoublePaymentException.class).handled(true).log(LoggingLevel.ERROR, "Double Payment not allowed")
                .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("400")).setBody().constant("Double Payment not allowed");

        onException(NotFoundException.class).handled(true)
                .setHeader(Exchange.CONTENT_TYPE, constant("text/plain")).log(LoggingLevel.ERROR, "Not Found")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("404")).setBody().constant("Not Found");

        onException(InSufficientFundsException.class).handled(true)
                .setHeader(Exchange.CONTENT_TYPE, constant("text/plain")).log(LoggingLevel.ERROR, "Insufficient Funds")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("400")).setBody().constant("Insufficient Funds");

        onException(InvalidReceiptTokenException.class).handled(true)
                .setHeader(Exchange.CONTENT_TYPE, constant("text/plain")).log(LoggingLevel.ERROR, "Invalid Receipt Token")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("400")).setBody().constant("Invalid Receipt Token");

        onException(InvalidAccountSenderException.class).handled(true)
                .setHeader(Exchange.CONTENT_TYPE, constant("text/plain")).log(LoggingLevel.ERROR, "Invalid Sender Account Id")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("400")).setBody().constant("Invalid Sender Account Id");

        onException(InvalidAccountRecipientException.class).handled(true)
                .setHeader(Exchange.CONTENT_TYPE, constant("text/plain")).log(LoggingLevel.ERROR, "Invalid Recipient Account Id")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("400")).setBody().constant("Invalid Recipient Account Id");

        onException(NegativePaymentAmountException.class).handled(true)
                .setHeader(Exchange.CONTENT_TYPE, constant("text/plain")).log(LoggingLevel.ERROR, "Invalid payment amount cannot be negative")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("400")).setBody().constant("Invalid payment amount cannot be negative");


        from("direct:submitPayment")
                .log("Validating payment order ${body.transactionId}")
                .process(exchange -> {
                    PaymentOrderDTO paymentOrderDTO = exchange.getIn().getBody(PaymentOrderDTO.class);
                    if(paymentOrderDTO.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                        throw new NegativePaymentAmountException();
                    }
                    TransactionDAO transactionDAO = (TransactionDAO) exchange.getContext().getRegistry().lookupByName("transactionDAO");
                    if (transactionDAO.exists(paymentOrderDTO.getTransactionId())) {
                        throw new DuplicateTransactionIdException();
                    }
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

                    Account recipient;
                    try {
                        recipient = accountDAO.find(paymentOrderDTO.getRecipientAccountId());
                    } catch (NoResultException e) {
                        throw new InvalidAccountRecipientException();
                    }

                    Account sender;
                    try {
                        sender = accountDAO.find(paymentOrderDTO.getSenderAccountId());
                    } catch (NoResultException e) {
                        throw new InvalidAccountSenderException();
                    }
                    logger.info("Submitting Payment Order to Transaction {}",transaction.getTransactionId());
                    transaction.setRecipient(recipient);
                    transaction.setSender(sender);
                    transaction.setPaymentOrderCreationDate(LocalDateTime.now());

                    balanceService.checkSenderBalance(transaction);

                    transaction.setStatus(TransactionStatus.INPROGRESS);


                    Receipt receipt = new Receipt();
                    receipt.setReceiptToken(UUID.randomUUID().toString());
                    receipt.setTransaction(transaction);
                    logger.info("Generating Receipt with Token {}", receipt.getReceiptToken());
                    paymentOrderDTO.setReceiptToken(receipt.getReceiptToken());
                    exchange.getIn().setBody(paymentOrderDTO);
                    transaction.setReceipt(receipt);

                    transactionDAO.save(transaction);
                });


        from("direct:finalizePayment")
                .log("Validating Finalization for transaction ${header.transactionId} with receipt token ${header.receiptToken}")
                .process(exchange -> {
                    String transactionId = exchange.getIn().getHeader("transactionId", String.class);
                    String receiptToken = exchange.getIn().getHeader("receiptToken", String.class);

                    ReceiptDAO receiptDAO = (ReceiptDAO) exchange.getContext().getRegistry().lookupByName("receiptDAO");
                    TransactionDAO transactionDAO = (TransactionDAO) exchange.getContext().getRegistry().lookupByName("transactionDAO");
                    LedgerDAO ledgerDao = (LedgerDAO) exchange.getContext().getRegistry().lookupByName("ledgerDAO");
                    if (!transactionDAO.exists(transactionId)) {
                        throw new NotFoundException();
                    }

                    Receipt receipt;
                    try {
                        receipt = receiptDAO.findByTransactionIdAndReceiptToken(transactionId, receiptToken);
                    } catch (NoResultException e) {
                        throw new InvalidReceiptTokenException();
                    }

                    Transaction transaction = receipt.getTransaction();


                    if(transaction.getStatus().equals(TransactionStatus.SUCCESFULL)) {
                        transaction.setPaymentReceiptCreationDate(LocalDateTime.now());
                        ledgerDao.findByTransactionId(transaction.getId()).forEach(ledgerUpdate -> {
                            ledgerUpdate.setStatus(LedgerUpdateStatus.FINAL);
                            ledgerDao.update(ledgerUpdate);
                        });
                        logger.info("Payment with id {} has been succesfully finalized", transactionId);
                    }

                    transactionDAO.update(transaction);
                    exchange.getIn().setBody(transaction);
                })
                .process(exchange -> {
                    Transaction transaction = exchange.getIn().getBody(Transaction.class);
                    ReceiptDTO receiptDTO = TransactionMapper.INSTANCE.transactionToReceiptDTO(transaction);
                    exchange.getIn().setBody(receiptDTO);
                });

        from("direct:enquirePaymentStatus")
                .process(exchange -> {
                    String applicationRefId = exchange.getIn().getHeader("applicationRefId", String.class);
                    TransactionDAO transactionDAO = (TransactionDAO) exchange.getContext().getRegistry().lookupByName("transactionDAO");
                    List<Transaction> transactionList;
                    try {
                        transactionList =  transactionDAO.findByApplicationRefId(applicationRefId);
                    } catch (NoResultException e) {
                        throw new NotFoundException();
                    }
                    List<ReceiptDTO> receiptDTOList = transactionList.stream().map(TransactionMapper.INSTANCE::transactionToReceiptDTO).collect(Collectors.toList());
                    exchange.getIn().setBody(receiptDTOList.size() > 1 ? receiptDTOList : receiptDTOList.get(0));
                });

    }

}
