package com.bizkredit.enums;

// Status of a collateral record
public enum CollateralStatus {
    REGISTERED,     // Collateral registered, not yet charged
    CHARGED,        // Charged against a facility
    RELEASED,       // Released after loan closure
    DISPUTED        // Under legal dispute
}
