package org.cgoro.db.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity(name = "ledger")
@Data
public class LedgerUpdate {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private String id;

    @ManyToOne
    private Account account;

    @Column(name = "balance_update")
    private BigDecimal balance_update;

    @OneToOne
    private Transaction transaction;
}
