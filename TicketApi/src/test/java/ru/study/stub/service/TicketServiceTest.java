package ru.study.stub.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


@Slf4j
class TicketServiceTest {

    private final Object pauseLock = new Object();
    private final AtomicBoolean cnt = new AtomicBoolean(true);

    @Test
    void testWait() {
        log.info("start");

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        executorService.schedule(() -> {
            synchronized (pauseLock) {
                log.info("start schedule task");
                cnt.set(false);
                pauseLock.notifyAll();
            }
        }, 5, TimeUnit.SECONDS);

        synchronized (pauseLock) {
            if (cnt.get()) {
                try {
                    log.info("start wait");
                    pauseLock.wait();
                    log.info("finish wait");
                } catch (InterruptedException ex) {
                    log.error("here");
                }
            }
        }

        log.info("finish");
    }

}