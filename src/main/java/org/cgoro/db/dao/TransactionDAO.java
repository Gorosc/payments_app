package org.cgoro.db.dao;

import org.cgoro.db.entity.TransactionStatus;
import org.cgoro.db.entity.Transaction;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;

public class TransactionDAO extends DAOImpl {

    @Inject
    TransactionDAO(EntityManager em) {
        this.em = em;
    }

    @Override
    public Transaction find(String transactionId) {
        return em.createNamedQuery("findByTransactionId", Transaction.class).setParameter("transactionId", transactionId).getSingleResult();
    }

    @Override
    public Transaction find(Long id) {
        return em.find(Transaction.class, id);
    }

    @Override
    public List getAll() {
        return em.createNamedQuery("allTransactions", Transaction.class).getResultList();
    }

    public Transaction findByApplicationRefIdAndStatus(String applicationRefId, TransactionStatus succesfull) {
        try {
            return em.createNamedQuery("findByApplicationRefIdAndStatus", Transaction.class).setParameter("applicationRefId", applicationRefId)
                    .setParameter("status", succesfull).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Transaction> findByStatus(TransactionStatus status) {
        return em.createNamedQuery("findByStatus", Transaction.class).setParameter("status", status).getResultList();
    }
}
