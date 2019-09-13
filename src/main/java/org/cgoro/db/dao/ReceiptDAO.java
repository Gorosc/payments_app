package org.cgoro.db.dao;

import org.cgoro.db.entity.Receipt;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

public class ReceiptDAO extends DAOImpl {

    @Inject
    ReceiptDAO(EntityManager em) {
        this.em = em;
    }

    @Override
    public Receipt find(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Receipt find(Long id) {
        return em.find(Receipt.class, id);
    }

    @Override
    public List<Receipt> getAll() {
        return em.createNamedQuery("allReceipts", Receipt.class).getResultList();
    }

    public Receipt findByTransactionIdAndReceiptToken(String transactionId, String receiptToken) {
        return em.createNamedQuery("findByTransactionIdAndReceiptToken",Receipt.class).setParameter("transactionId",transactionId)
                .setParameter("receiptToken", receiptToken).getSingleResult();
    }
}
