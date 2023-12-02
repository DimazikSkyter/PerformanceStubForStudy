package ru.nspk.performance.transactionshandler.transformer;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.text.ParseException;
import java.util.Map;

@RequiredArgsConstructor
public class TransformerMultiton {

    private final Map<Class, Transformer> transformers;

    public <I, O> O transform(@NonNull I in) throws ParseException {
        return (O) transformers.get(in.getClass()).transform(in);
    }

    public <I, O> O transform(@NonNull String in, Class<I> cls) throws ParseException {
        return (O) transformers.get(cls).transform(in);
    }
}
