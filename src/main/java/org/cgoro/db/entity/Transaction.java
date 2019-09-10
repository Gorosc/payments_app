package org.cgoro.db.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
public class Transaction {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private String id;

    @Column(name = "transaction_id", unique = true)
    private String transactionId;

    @Column(name = "app_ref_id")
    private String applicationRefId;

    @Column(name = "payment_id", unique = true)
    private String paymentId;

    @ManyToOne
    @JoinColumn
    private Account sender;

    @ManyToOne
    @JoinColumn
    private Account recipient;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "po_create_date")
    private LocalDateTime paymentOrderCreationDate;

    @Column(name = "pr_create_date")
    private LocalDateTime paymentReceiptCreationDate;

    @OneToOne
    private Receipt receipt;

    @Column(name = "processed")
    private boolean processed;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10)
    private Status status;

    @Column(name = "reason")
    private String reason;

}
