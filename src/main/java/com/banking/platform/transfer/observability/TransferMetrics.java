package com.banking.platform.transfer.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class TransferMetrics {

    private final Counter started;
    private final Counter completed;
    private final Counter failed;
    private final Counter retried;
    private final Counter sentToDlt;

    private final Timer duration;

    public TransferMetrics(MeterRegistry registry) {

        this.started = Counter.builder("transfer.started")
                .description("Transfers started")
                .register(registry);

        this.completed = Counter.builder("transfer.completed")
                .description("Transfers completed successfully")
                .register(registry);

        this.failed = Counter.builder("transfer.failed")
                .description("Transfers failed")
                .register(registry);

        this.retried = Counter.builder("transfer.retried")
                .description("Transfer retries")
                .register(registry);

        this.sentToDlt = Counter.builder("transfer.dlt")
                .description("Transfers sent to DLT")
                .register(registry);

        this.duration = Timer.builder("transfer.duration")
                .description("Transfer end-to-end duration")
                .register(registry);
    }

    public void started() {
        started.increment();
    }

    public void completed() {
        completed.increment();
    }

    public void failed() {
        failed.increment();
    }

    public void retried() {
        retried.increment();
    }

    public void sentToDlt() {
        sentToDlt.increment();
    }

    public Timer.Sample startTimer() {
        return Timer.start();
    }

    public void stopTimer(Timer.Sample sample) {
        sample.stop(duration);
    }
}
