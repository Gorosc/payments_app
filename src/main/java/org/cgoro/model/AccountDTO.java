package org.cgoro.model;

import java.time.LocalDateTime;

public class AccountDTO{

    private String accountId;
    private String nameEn;
    private LocalDateTime creationDate;


    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public String toString() {
        return "AccountDTO{" +
                "accountId='" + accountId + '\'' +
                ", nameEn='" + nameEn + '\'' +
                ", creationDate=" + creationDate +
                '}';
    }
}
