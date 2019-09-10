package org.cgoro.db.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class Receipt {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private String id;

    @OneToOne(mappedBy = "receipt", cascade = CascadeType.ALL)
    Transaction transaction;

    @Column(name = "receipt_token")
    private String receiptToken;


}
