package org.cgoro.model;

public class PaymentOrder extends Dto{

    private String transactionId;
    private String appRefId;
    private String senderAccountId;
    private String recipientAccountId;
    private String amount;
    private String receiptToken;

    public String getTransactionId() {
        return transactionId;
    }

    public PaymentOrder setTransactionId(String transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    public String getAppRefId() {
        return appRefId;
    }

    public PaymentOrder setAppRefId(String appRefId) {
        this.appRefId = appRefId;
        return this;
    }

    public String getSenderAccountId() {
        return senderAccountId;
    }

    public PaymentOrder setSenderAccountId(String senderAccountId) {
        this.senderAccountId = senderAccountId;
        return this;
    }

    public String getRecipientAccountId() {
        return recipientAccountId;
    }

    public PaymentOrder setRecipientAccountId(String recipientAccountId) {
        this.recipientAccountId = recipientAccountId;
        return this;
    }

    public String getAmount() {
        return amount;
    }

    public PaymentOrder setAmount(String amount) {
        this.amount = amount;
        return this;
    }

    public String getReceiptToken() {
        return receiptToken;
    }

    public PaymentOrder setReceiptToken(String receiptToken) {
        this.receiptToken = receiptToken;
        return this;
    }
}
