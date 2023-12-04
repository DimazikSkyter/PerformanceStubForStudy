package ru.nspk.performance.transactionshandler.transformer;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ru.nspk.performance.transactionshandler.state.TicketTransactionState;

import java.text.ParseException;
import java.util.Map;

@RequiredArgsConstructor
public class TransformerMultiton {

    private final Map<Class, Transformer> transformers;

    public <I, O> O transform(@NonNull I in, @NonNull Class<O> out) throws ParseException, JsonProcessingException {
        return (O) transformers.get(out).transform(in);
    }

    public <I, O> O transform(@NonNull I in) throws ParseException, JsonProcessingException {
        return (O) transformers.get(in.getClass()).transform(in);
    }

    public <O> O transform(@NonNull String in, Class cls) throws ParseException, JsonProcessingException {
        return (O) transformers.get(cls).transform(in);
    }
}
