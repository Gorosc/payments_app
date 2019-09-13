package org.cgoro.db.entity;

import java.util.Arrays;
import java.util.List;

public enum  LedgerUpdateStatus {
    PENDING,
    FINAL,
    CANCELLED,
    REFUNDED;

    public static List<LedgerUpdateStatus> getBalanceSignificantStatuses() {
        return Arrays.asList(PENDING, FINAL);
    }
}
