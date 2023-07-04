package ru.nspk.performance.theatre.exception;

public class PurchaseException extends RuntimeException {

    public PurchaseException(long reserveId, String reason) {
        super("Failed to make a purchase for reserveId " + reserveId + " for reason " + reason);
    }
}
