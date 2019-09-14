package org.cgoro.db.entity;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Entity that implements the ledger. The global truth about balances.
 * Each ledger update is map to an account and to a transaction and adds or subtracts an amount
 * to the account
 */
@Entity(name = "ledger")
@NamedQueries({
        @NamedQuery(name = "getAccountAll", query = "select x from ledger x where x.account.accountId= :accountId AND x.status IN (:statuses)"),
        @NamedQuery(name = "findLedgerUpdatesByTransactionId", query = "select x from ledger x where x.transaction.id = :transactionId")
})
public class LedgerUpdate {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private Account account;

    @Column(name = "balance_update", precision = 8, scale = 2)
    private BigDecimal balanceUpdate;

    @Enumerated
    @Column(name = "status")
    private LedgerUpdateStatus status;

    @OneToOne
    private Transaction transaction;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public BigDecimal getBalanceUpdate() {
        return balanceUpdate;
    }

    public void setBalanceUpdate(BigDecimal balanceUpdate) {
        this.balanceUpdate = balanceUpdate;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public LedgerUpdateStatus getStatus() {
        return status;
    }

    public void setStatus(LedgerUpdateStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "LedgerUpdate{" +
                "id=" + id +
                ", account=" + account +
                ", balanceUpdate=" + balanceUpdate +
                ", status=" + status +
                ", transaction=" + transaction +
                '}';
    }
}
