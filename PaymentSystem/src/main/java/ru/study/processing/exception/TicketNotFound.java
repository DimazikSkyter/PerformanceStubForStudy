package ru.study.processing.exception;

public class TicketNotFound extends NullPointerException {

    private final String ticketUid ;

    public TicketNotFound(String s) {
        super("Ticket " + s + " not found");
        this.ticketUid = s;
    }
}
