package com.banking.platform.listener;

import com.banking.platform.event.MoneyTransferredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TransferEventListener {

    private static final Logger log = LoggerFactory.getLogger(TransferEventListener.class);

    //Listeners handle side effects and are easily replaceable by Kafka consumers
    @EventListener
    public void handleMoneyTransfer(MoneyTransferredEvent event){
        log.info("Money transferred from {} to {} | Amount={}",
                event.getFromAccount(),event.getToAccount(),event.getAmount());
    }
}
