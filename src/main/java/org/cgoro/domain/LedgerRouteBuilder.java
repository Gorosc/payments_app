package org.cgoro.domain;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.cgoro.db.dao.LedgerDAO;
import org.cgoro.db.dao.TransactionDAO;
import org.cgoro.db.entity.*;
import org.cgoro.exception.InSufficientFundsException;
import org.cgoro.exception.LedgerConcurrentException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class LedgerRouteBuilder extends RouteBuilder {

    /*
    * Semaphore to implement sequential ledger updates
    * */
    private Semaphore processing = new Semaphore(1);

    @Override
    public void configure() throws Exception {

        onException(LedgerConcurrentException.class).handled(true).log(LoggingLevel.WARN, "Ledger is currently processing");
        onException(InSufficientFundsException.class).handled(true).log(LoggingLevel.WARN, "Insufficient funds transaction with id ${body.transactionId}");

        from("timer://ledgerProcess?fixedRate=true&period=1s")
                .process(exchange -> {
                    //Acquire the lock otherwise throw an exception and exit
                    if (!processing.tryAcquire()) {
                        throw new LedgerConcurrentException();
                    }
                    TransactionDAO transactionDAO = (TransactionDAO) exchange.getContext().getRegistry().lookupByName("transactionDAO");

                    List<Transaction> transactionList = transactionDAO.findByStatus(TransactionStatus.INPROGRESS).stream().peek(transaction -> transaction.setStatus(TransactionStatus.PROCESSING)).collect(Collectors.toList());
                    transactionDAO.updateAll(transactionList);
                    exchange.getIn().setBody(transactionList);
                })
                .split(body()).streaming().to("direct:processTransaction").end()
                .process(exchange -> {
                    processing.release();
                });


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
