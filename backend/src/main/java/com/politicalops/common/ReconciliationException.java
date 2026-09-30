package com.politicalops.common;

/** Raised when an election import fails a data quality rule (vote sums, duplicate parts, etc). */
public class ReconciliationException extends RuntimeException {
    public ReconciliationException(String message) { super(message); }
}
