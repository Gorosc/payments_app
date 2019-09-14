package org.cgoro.db.entity;

import java.util.Arrays;
import java.util.List;

public enum  LedgerUpdateStatus {
    PENDING,
    FINAL,
    CANCELLED,
    REFUNDED;

    /**
     * @return Balance Statuses that count towards an account's balance
     */
    public static List<LedgerUpdateStatus> getBalanceSignificantStatuses() {
        return Arrays.asList(PENDING, FINAL);
    }
}
