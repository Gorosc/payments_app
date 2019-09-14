package org.cgoro.domain;

import org.cgoro.db.dao.LedgerDAO;
import org.cgoro.db.dao.TransactionDAO;
import org.cgoro.db.entity.Account;
import org.cgoro.db.entity.LedgerUpdate;
import org.cgoro.db.entity.Transaction;
import org.cgoro.db.entity.TransactionStatus;
import org.cgoro.exception.InSufficientFundsException;

import java.math.BigDecimal;

/**
 * Balance related methods
 */
public class BalanceService {

    private LedgerDAO ledgerDAO;
    private TransactionDAO transactionDAO;

    public BalanceService(LedgerDAO ledgerDAO, TransactionDAO transactionDAO) {
        this.ledgerDAO = ledgerDAO;
        this.transactionDAO = transactionDAO;
    }

    void checkSenderBalance(Transaction transaction) throws InSufficientFundsException {

        Account sender = transaction.getSender();
        BigDecimal amount = transaction.getAmount();

        BigDecimal balance = getAccountBalance(sender);
        if (0 < amount.compareTo(balance)) {
            transaction.setStatus(TransactionStatus.UNSUCCESFULL);
            transaction.setReason("Insufficient Funds");
            transactionDAO.update(transaction);
            throw new InSufficientFundsException();
        }
    }

    BigDecimal getAccountBalance(Account account) {
        return ledgerDAO.getAllBalanceSignificant(account.getAccountId()).stream().map(LedgerUpdate::getBalanceUpdate).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
