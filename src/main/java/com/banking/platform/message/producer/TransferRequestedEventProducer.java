package com.banking.platform.message.producer;

import com.banking.platform.transfer.event.TransferRequestedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TransferRequestedEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public TransferRequestedEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(TransferRequestedEvent event) {
        kafkaTemplate.send(
                "transfer.requested",
                event.transferId().toString(),
                event
        );

        log.info(
                "PRODUCING transfer.requested transferId={}",
                event.transferId()
        );
    }
}
