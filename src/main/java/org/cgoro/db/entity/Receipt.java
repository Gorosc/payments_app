package org.cgoro.db.entity;

import javax.persistence.*;

/**
 * Entity to store the receipts for the payments
 * TODO: More business information could be stored in the receipt
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "allReceipts", query = "select x from Receipt x"),
        @NamedQuery(name = "findByTransactionIdAndReceiptToken", query = "select x from Receipt x where x.receiptToken = :receiptToken AND x.transaction.transactionId = :transactionId")
})
public class Receipt {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @OneToOne
    private Transaction transaction;

    @Column(name = "receipt_token")
    private String receiptToken;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public String getReceiptToken() {
        return receiptToken;
    }

    public void setReceiptToken(String receiptToken) {
        this.receiptToken = receiptToken;
    }

    @Override
    public String toString() {
        return "Receipt{" +
                "id='" + id + '\'' +
                ", transaction=" + transaction +
                ", receiptToken='" + receiptToken + '\'' +
                '}';
    }
}
