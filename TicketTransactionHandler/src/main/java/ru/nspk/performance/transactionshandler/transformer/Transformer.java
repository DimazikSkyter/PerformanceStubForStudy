package ru.nspk.performance.transactionshandler.transformer;

import lombok.NonNull;

import java.text.ParseException;

public interface Transformer<I, O>{

    @NonNull O transform(I in) throws ParseException;
}
