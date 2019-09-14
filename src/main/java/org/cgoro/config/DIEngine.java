package org.cgoro.config;

import dagger.Component;
import org.cgoro.db.DBManager;
import org.cgoro.db.dao.AccountDAO;
import org.cgoro.db.dao.LedgerDAO;
import org.cgoro.db.dao.ReceiptDAO;
import org.cgoro.db.dao.TransactionDAO;

import javax.inject.Singleton;

/**
 * Class tha consists the context of application creating instances of DI objects
 */
@Singleton
@Component(modules = ConfigModule.class)
public interface DIEngine {
    DBManager dbManager();
    AccountDAO accountDAO();
    TransactionDAO transactionDAO();
    ReceiptDAO receiptDAO();
    LedgerDAO ledgerDAO();
}

