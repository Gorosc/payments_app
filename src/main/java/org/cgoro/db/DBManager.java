package  org.cgoro.db;

import org.cgoro.db.entity.Account;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.swing.text.html.parser.Entity;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

public class DBManager {

    private static Logger logger = LoggerFactory.getLogger(DBManager.class);

    private EntityManager em;

    @Inject
    DBManager(EntityManager em) {
        this.em = em;
    }

    private static void startDB() throws SQLException {
        Server.createTcpServer("-tcpPort", "9092", "-tcpAllowOthers", "-ifNotExists").start();
    }

    private static void stopDB() throws SQLException {
        Server.shutdownTcpServer("tcp://localhost:9092", "", true, true);
    }

    public static void start() {
        ((Runnable) () -> {
            try {
                startDB();
                Class.forName("org.h2.Driver");
                Connection conn = DriverManager.
                        getConnection("jdbc:h2:tcp://localhost:9092/~/demoapp", "sa", "");
                System.out.println("Connection Established: "
                        + conn.getMetaData().getDatabaseProductName() + "/" + conn.getCatalog());
            } catch (SQLException e) {
                logger.error("Cannot create Database", e);
            } catch (ClassNotFoundException e) {
                logger.error("Missing Dependency", e);
            }
        }).run();
    }

    public void init() {
        Account account1 = new Account();
        account1.setNameEn("John Lock");
        account1.setAccountId(UUID.randomUUID().toString());
        account1.setCreationDate(LocalDateTime.now());

        Account account2 = new Account();
        account2.setNameEn("Jane Bounce");
        account2.setAccountId(UUID.randomUUID().toString());
        account2.setCreationDate(LocalDateTime.now());

        em.getTransaction().begin();
        em.persist(account1);
        em.persist(account2);
        em.getTransaction().commit();
    }
}
