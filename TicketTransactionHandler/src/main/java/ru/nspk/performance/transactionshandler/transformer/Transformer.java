package ru.nspk.performance.transactionshandler.transformer;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NonNull;

import java.text.ParseException;

public interface Transformer<I, O>{

    @NonNull O transform(I in) throws ParseException, JsonProcessingException;
}
