package org.cgoro.model;

import org.cgoro.db.entity.TransactionStatus;

import java.math.BigDecimal;

public class ReceiptDTO {

    private String transactionId;
    private String applicationRefId;
    private String paymentId;
    private TransactionStatus status;
    private BigDecimal amount;


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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ReceiptDTO{" +
                "transactionId='" + transactionId + '\'' +
                ", applicationRefId='" + applicationRefId + '\'' +
                ", paymentId='" + paymentId + '\'' +
                ", status=" + status +
                ", amount=" + amount +
                '}';
    }
}
