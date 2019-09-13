package org.cgoro;

import org.apache.camel.main.Main;
import org.cgoro.config.ConfigModule;
import org.cgoro.config.DIEngine;
import org.cgoro.config.DaggerDIEngine;
import org.cgoro.db.DBManager;
import org.cgoro.domain.*;
import org.cgoro.rest.RestRouteBuilder;

/**
 * A Camel Application
 */
public class MainApp {

    static DIEngine di;
    static Main main;

    /**
     * A main() so we can easily run these routing rules in our IDE
     */
    public static void main(String... args) throws Exception {
        DBManager.start();

        DIEngine diEngine = DaggerDIEngine.builder().configModule(new ConfigModule()).build();
        di = diEngine;
        diEngine.dbManager().init();

        main = new Main();
        main.bind("em", diEngine.dbManager().getEm());
        main.bind("accountDAO", diEngine.accountDAO());
        main.bind("transactionDAO", diEngine.transactionDAO());
        main.bind("receiptDAO", diEngine.receiptDAO());
        main.bind("ledgerDAO", diEngine.ledgerDAO());
        main.bind("balanceService", new BalanceService(diEngine.ledgerDAO(), diEngine.transactionDAO()));
        main.addRouteBuilder(new RestRouteBuilder());
        main.addRouteBuilder(new PaymentsRouteBuilder());
        main.addRouteBuilder(new AccountsRouteBuilder());
        main.addRouteBuilder(new LedgerRouteBuilder());
        main.run(args);
    }

}

