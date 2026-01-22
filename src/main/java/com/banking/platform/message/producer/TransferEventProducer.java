package com.banking.platform.message.producer;

import com.banking.platform.domain.event.TransferCompletedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class TransferEventProducer {

    private final KafkaTemplate<String, TransferCompletedEvent> kafkaTemplate;

    public TransferEventProducer(KafkaTemplate<String, TransferCompletedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(TransferCompletedEvent event){
        kafkaTemplate.send(
                "transfer.events",
                event.getTransferId(),
                event
        );
    }
}
