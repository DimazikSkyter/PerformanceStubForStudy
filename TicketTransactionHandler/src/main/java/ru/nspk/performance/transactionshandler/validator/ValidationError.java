package ru.nspk.performance.transactionshandler.validator;

public enum ValidationError {
    DUPLICATE, USER_NOT_FOUND, EVENT_NOT_FOUND, WRONG_EVENT_DATE_FORMAT, EVENT_DATE_EXPIRED, REQUEST_ID_IS_NULL, FAILED_TO_RESOLVE_PAYMENT_STATUS
}
