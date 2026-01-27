package com.banking.platform.common.idempotency;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class EventIdempotencyGuard {

    private final ProcessedEventRepository repository;

    //READ-ONLY check
    public boolean alreadyProcessed(String eventId) {
        return repository.existsById(eventId);
    }

    //WRITE happens ONLY after success
    public void markProcessed(String eventId, String eventType) {
        repository.save(new ProcessedEvent(eventId, eventType));
    }
}


