package org.cgoro.db.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity that implements a a transaction initiated by a payment order. When successful a
 * transaction will lead in a balance update. When not it is a lof of the attempt and the failure.
 * The transaction also holds  more information required to track and investigate payments like
 * the payment order and receipt generation times
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "allTransactions", query = "select x from Transaction x"),
        @NamedQuery(name = "findByTransactionId",query = "select x from Transaction x where x.transactionId = :transactionId"),
        @NamedQuery(name = "findByApplicationRefIdAndStatus",query = "select x from Transaction x where x.applicationRefId = :applicationRefId AND x.status = :status"),
        @NamedQuery(name = "findByStatus", query = "select x from Transaction x where x.status = :status"),
        @NamedQuery(name = "findByApplicationRefId", query = "select x from Transaction x where x.applicationRefId = :applicationRefId"),
})
public class Transaction {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @Column(name = "transaction_id", unique = true)
    private String transactionId;

    @Column(name = "app_ref_id")
    private String applicationRefId;

    @Column(name = "payment_id", unique = true)
    private String paymentId;

    @ManyToOne
    @JoinColumn(name = "sender_id", referencedColumnName = "id")
    private Account sender;

    @ManyToOne
    @JoinColumn(name = "recipient_id", referencedColumnName = "id")
    private Account recipient;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "po_create_date")
    private LocalDateTime paymentOrderCreationDate;

    @Column(name = "pr_create_date")
    private LocalDateTime paymentReceiptCreationDate;

    @OneToOne(mappedBy = "transaction", cascade = CascadeType.ALL)
    private Receipt receipt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private TransactionStatus status;

    @Column(name = "reason")
    private String reason;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getApplicationRefId() {
        return applicationRefId;
    }

    public void setApplicationRefId(String applicationRefId) {
        this.applicationRefId = applicationRefId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public Account getSender() {
        return sender;
    }

    public void setSender(Account sender) {
        this.sender = sender;
    }

    public Account getRecipient() {
        return recipient;
    }

    public void setRecipient(Account recipient) {
        this.recipient = recipient;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getPaymentOrderCreationDate() {
        return paymentOrderCreationDate;
    }

    public void setPaymentOrderCreationDate(LocalDateTime paymentOrderCreationDate) {
        this.paymentOrderCreationDate = paymentOrderCreationDate;
    }

    public LocalDateTime getPaymentReceiptCreationDate() {
        return paymentReceiptCreationDate;
    }

    public void setPaymentReceiptCreationDate(LocalDateTime paymentReceiptCreationDate) {
        this.paymentReceiptCreationDate = paymentReceiptCreationDate;
    }

    public Receipt getReceipt() {
        return receipt;
    }

    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
