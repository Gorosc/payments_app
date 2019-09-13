package org.cgoro.db.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NamedQueries({
        @NamedQuery(name = "allAccounts", query = "select x from Account x"),
        @NamedQuery(name = "findByAccountId", query = "select x from Account x where x.accountId = :accountId")
})
public class Account {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @Column(name = "account_id", unique = true)
    private String accountId;

    @Column(name = "name_en")
    private String nameEn;

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Transaction> outgoingTransactions;

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Transaction> incomingTransactions;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<LedgerUpdate> ledgerUpdates;

    @Column(name = "account_create_dt")
    private LocalDateTime creationDate;


    public String getAccountId() {
        return accountId;
    }

    public Account setAccountId(String accountId) {
        this.accountId = accountId;
        return this;
    }

    public String getNameEn() {
        return nameEn;
    }

    public Account setNameEn(String nameEn) {
        this.nameEn = nameEn;
        return this;
    }

    public List<Transaction> getOutgoingTransactions() {
        return outgoingTransactions;
    }

    public Account setOutgoingTransactions(List<Transaction> outgoingTransactions) {
        this.outgoingTransactions = outgoingTransactions;
        return this;
    }

    public List<Transaction> getIncomingTransactions() {
        return incomingTransactions;
    }

    public Account setIncomingTransactions(List<Transaction> incomingTransactions) {
        this.incomingTransactions = incomingTransactions;
        return this;
    }

    public List<LedgerUpdate> getLedgerUpdates() {
        return ledgerUpdates!=null?ledgerUpdates:new ArrayList<>();
    }

    public Account setLedgerUpdates(List<LedgerUpdate> ledgerUpdates) {
        this.ledgerUpdates = ledgerUpdates;
        return this;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public Account setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
        return this;
    }
}
