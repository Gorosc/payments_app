package org.cgoro;

import org.apache.camel.main.Main;
import org.cgoro.config.ConfigModule;
import org.cgoro.config.DIEngine;
import org.cgoro.config.DaggerDIEngine;
import org.cgoro.db.DBManager;
import org.cgoro.domain.*;
import org.cgoro.rest.RestRouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Camel Application
 * The application status is first initialized DB - Context - Routes and then started waiting connections until stopped
 * externally.
 */
public class MainApp {

    static DIEngine di;
    static Main main;
    static Logger logger = LoggerFactory.getLogger(MainApp.class);

    public static void main(String... args) throws Exception {
        DBManager.start();

        DIEngine diEngine = DaggerDIEngine.builder().configModule(new ConfigModule()).build();
        di = diEngine;
        diEngine.dbManager().init();

        main = new Main();
        logger.info("Injecting Beans");
        main.bind("em", diEngine.dbManager().getEm());
        main.bind("accountDAO", diEngine.accountDAO());
        main.bind("transactionDAO", diEngine.transactionDAO());
        main.bind("receiptDAO", diEngine.receiptDAO());
        main.bind("ledgerDAO", diEngine.ledgerDAO());
        main.bind("balanceService", new BalanceService(diEngine.ledgerDAO(), diEngine.transactionDAO()));
        logger.info("Injecting Routes");
        main.addRouteBuilder(new RestRouteBuilder());
        main.addRouteBuilder(new PaymentsRouteBuilder());
        main.addRouteBuilder(new AccountsRouteBuilder());
        main.addRouteBuilder(new LedgerRouteBuilder());
        main.run(args);
    }

}

