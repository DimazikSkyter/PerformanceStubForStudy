package ru.nspk.performance.transactionshandler.timeoutprocessor;

import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class TimeoutProcessor {

    private final ScheduledExecutorService executorService;

    public void executeWithTimeout(Runnable runnable, Duration timeout, Runnable runnableAfterTimeout) {
        executorService.execute(runnable);
        executorService.schedule(runnableAfterTimeout, timeout.toMillis(), TimeUnit.MILLISECONDS);
    }
}
