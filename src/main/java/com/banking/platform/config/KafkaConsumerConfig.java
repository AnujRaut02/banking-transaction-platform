package com.banking.platform.config;


import com.banking.platform.exception.ValidationFailureException;
import com.banking.platform.transfer.observability.TransferMetrics;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object>
    kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory,
            KafkaTemplate<String, Object> kafkaTemplate,
            TransferMetrics metrics
    ) {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        //  DLT handler (THIS is where DLT metric belongs)
        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (record, ex) -> {
                            metrics.sentToDlt(); //  CORRECT PLACE
                            log.error(
                                    "Sending record to DLT topic={} partition={} offset={}",
                                    record.topic(),
                                    record.partition(),
                                    record.offset(),
                                    ex
                            );
                            return new TopicPartition("transfer.requested.DLT", record.partition()
                            );
                        }
                );

        DefaultErrorHandler errorHandler =
                new DefaultErrorHandler(
                        recoverer,
                        new FixedBackOff(3000L, 3) // 3 retries
                );

        //  NO retries for business exceptions
        errorHandler.addNotRetryableExceptions(
                IllegalStateException.class
        );


        //  Retry metric (this is correct)
        errorHandler.setRetryListeners((record, ex, attempt) -> {
            metrics.retried();
            log.warn(
                    "Retry attempt={} topic={} offset={}",
                    attempt,
                    record.topic(),
                    record.offset()
            );
        });

        factory.setCommonErrorHandler(errorHandler);
        factory.getContainerProperties()
                .setAckMode(ContainerProperties.AckMode.MANUAL);

        return factory;
    }
}
