package  org.cgoro.db;

import org.cgoro.db.entity.Account;
import org.cgoro.db.entity.LedgerUpdate;
import org.cgoro.db.entity.LedgerUpdateStatus;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class DBManager {

    private static Logger logger = LoggerFactory.getLogger(DBManager.class);

    private EntityManagerFactory emf;
    private EntityManager em;

    @Inject
    DBManager(EntityManagerFactory emf, EntityManager em) {
        this.em = em;
        this.emf = emf;
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
                logger.info("Connection Established: "
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
        account1.setAccountId("ACCOUNT1");
        account1.setCreationDate(LocalDateTime.now());

        Account account2 = new Account();
        account2.setNameEn("Jane Bounce");
        account2.setAccountId("ACCOUNT2");
        account2.setCreationDate(LocalDateTime.now());

        LedgerUpdate ledgerUpdate = new LedgerUpdate();
        ledgerUpdate.setAccount(account1);
        ledgerUpdate.setBalanceUpdate(BigDecimal.valueOf(50000));
        ledgerUpdate.setStatus(LedgerUpdateStatus.FINAL);

        LedgerUpdate ledgerUpdate2 = new LedgerUpdate();
        ledgerUpdate2.setAccount(account2);
        ledgerUpdate2.setBalanceUpdate(BigDecimal.valueOf(50000));
        ledgerUpdate2.setStatus(LedgerUpdateStatus.FINAL);

        account1.getLedgerUpdates().add(ledgerUpdate);
        account2.getLedgerUpdates().add(ledgerUpdate2);

        em.getTransaction().begin();
        em.persist(account1);
        em.persist(ledgerUpdate);
        em.persist(account2);
        em.persist(ledgerUpdate2);
        em.getTransaction().commit();
    }

    public EntityManagerFactory getEmf() {
        return emf;
    }

    public void setEmf(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public EntityManager getEm() {
        return em;
    }

    public void setEm(EntityManager em) {
        this.em = em;
    }

}
