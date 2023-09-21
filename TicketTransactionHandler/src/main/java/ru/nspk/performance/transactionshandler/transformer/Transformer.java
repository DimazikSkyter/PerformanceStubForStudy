package ru.nspk.performance.transactionshandler.transformer;

import lombok.NonNull;

public interface Transformer<I, O>{

    @NonNull O transform(I in);
}
