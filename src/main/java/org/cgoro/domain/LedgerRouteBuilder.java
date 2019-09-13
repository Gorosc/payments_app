package org.cgoro.domain;

import org.apache.camel.builder.RouteBuilder;
import org.cgoro.db.dao.LedgerDAO;
import org.cgoro.db.dao.TransactionDAO;
import org.cgoro.db.entity.*;
import org.cgoro.exception.InSufficientFundsException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class LedgerRouteBuilder extends RouteBuilder {
    @Override
    public void configure() throws Exception {

        from("timer://ledgerProcess?fixedRate=true&period=1s")
                .process(exchange -> {
                    TransactionDAO transactionDAO = (TransactionDAO) exchange.getContext().getRegistry().lookupByName("transactionDAO");

                    List<Transaction> transactionList = transactionDAO.findByStatus(TransactionStatus.INPROGRESS);
                    exchange.getIn().setBody(transactionList);
                })
                .split(body()).streaming().to("direct:processTransaction");


        from("direct:processTransaction")
                .id("ledger")
                .log("Processing ${body.transactionId}")
                .process(exchange -> {
                    Transaction transaction = exchange.getIn().getBody(Transaction.class);

                    TransactionDAO transactionDAO = (TransactionDAO) exchange.getContext().getRegistry().lookupByName("transactionDAO");
                    LedgerDAO ledgerDao = (LedgerDAO) exchange.getContext().getRegistry().lookupByName("ledgerDAO");
                    BalanceService balanceService = (BalanceService) exchange.getContext().getRegistry().lookupByName("balanceService");

                    Account sender = transaction.getSender();
                    Account recipient = transaction.getRecipient();
                    BigDecimal amount = transaction.getAmount();

                    balanceService.checkSenderBalance(transaction);

                    LedgerUpdate ledgerUpdateSender = new LedgerUpdate();
                    ledgerUpdateSender.setAccount(sender);
                    ledgerUpdateSender.setBalanceUpdate(amount.negate());
                    ledgerUpdateSender.setTransaction(transaction);
                    ledgerUpdateSender.setStatus(LedgerUpdateStatus.PENDING);

                    LedgerUpdate ledgerUpdateRecipient = new LedgerUpdate();
                    ledgerUpdateRecipient.setAccount(recipient);
                    ledgerUpdateRecipient.setBalanceUpdate(amount);
                    ledgerUpdateRecipient.setTransaction(transaction);
                    ledgerUpdateRecipient.setStatus(LedgerUpdateStatus.PENDING);

                    ledgerDao.save(ledgerUpdateSender, ledgerUpdateRecipient);

                    transaction.setPaymentId(UUID.randomUUID().toString());
                    transaction.setStatus(TransactionStatus.SUCCESFULL);
                    transactionDAO.update(transaction);
                });

    }
}
