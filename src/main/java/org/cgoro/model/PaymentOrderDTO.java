package org.cgoro.model;

import java.math.BigDecimal;

public class PaymentOrderDTO{

    private String transactionId;
    private String applicationRefId;
    private String senderAccountId;
    private String recipientAccountId;
    private BigDecimal amount;
    private String receiptToken;

    public String getTransactionId() {
        return transactionId;
    }

    public PaymentOrderDTO setTransactionId(String transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    public String getApplicationRefId() {
        return applicationRefId;
    }

    public PaymentOrderDTO setApplicationRefId(String applicationRefId) {
        this.applicationRefId = applicationRefId;
        return this;
    }

    public String getSenderAccountId() {
        return senderAccountId;
    }

    public PaymentOrderDTO setSenderAccountId(String senderAccountId) {
        this.senderAccountId = senderAccountId;
        return this;
    }

    public String getRecipientAccountId() {
        return recipientAccountId;
    }

    public PaymentOrderDTO setRecipientAccountId(String recipientAccountId) {
        this.recipientAccountId = recipientAccountId;
        return this;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentOrderDTO setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public String getReceiptToken() {
        return receiptToken;
    }

    public PaymentOrderDTO setReceiptToken(String receiptToken) {
        this.receiptToken = receiptToken;
        return this;
    }
}
