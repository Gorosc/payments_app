package org.cgoro;

import org.apache.camel.main.Main;
import org.cgoro.db.DBManager;
import org.cgoro.config.ConfigModule;
import org.cgoro.config.DIEngine;
import org.cgoro.config.DaggerDIEngine;
import org.cgoro.rest.RestPaymentRouteBuilder;

/**
 * A Camel Application
 */
public class MainApp {

    /**
     * A main() so we can easily run these routing rules in our IDE
     */
    public static void main(String... args) throws Exception {
        DBManager.start();

        DIEngine diEngine = DaggerDIEngine.builder().configModule(new ConfigModule()).build();
        diEngine.dbManager().init();

        Main main = new Main();
        main.addRouteBuilder(new MyRouteBuilder());
        main.addRouteBuilder(new RestPaymentRouteBuilder());
        main.run(args);
    }

}

