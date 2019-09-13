package org.cgoro.db.dao;

import org.cgoro.db.entity.*;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

public class LedgerDAO extends DAOImpl {

    @Inject
    LedgerDAO(EntityManager em) {
        this.em = em;
    }

    @Override
    public Object find(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LedgerUpdate find(Long id) {
        return em.find(LedgerUpdate.class, id);
    }

    @Override
    public List getAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void save(Object entity) {
        throw new IllegalStateException("LedgerUpdates are only saved in pairs");
    }

    public void save(LedgerUpdate updateSender, LedgerUpdate recipient) {
        em.getTransaction().begin();
        em.persist(updateSender);
        em.persist(recipient);
        em.getTransaction().commit();
    }

    public List<LedgerUpdate> getAll(String accountId) {
        return em.createNamedQuery("getAccountAll", LedgerUpdate.class).setParameter("accountId", accountId)
                .setParameter("statuses", LedgerUpdateStatus.getBalanceSignificantStatuses())
                .getResultList();
    }

    public List<LedgerUpdate> findByTransactionId(Long transactionId) {
        List<LedgerUpdate> ledgerUpdates = em.createNamedQuery("findLedgerUpdatesByTransactionId",LedgerUpdate.class)
                .setParameter("transactionId", transactionId)
                .getResultList();
        if (!ledgerUpdates.isEmpty() && ledgerUpdates.size() != 2) {
            throw new IllegalStateException("2 Ledger Updated should be registered for each transaction");
        }
        return ledgerUpdates;
    }
}
