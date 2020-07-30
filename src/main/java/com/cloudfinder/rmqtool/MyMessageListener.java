package com.cloudfinder.rmqtool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.TextMessage;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static com.cloudfinder.rmqtool.MessagingRabbitmqApplication.counter;
import static com.cloudfinder.rmqtool.MessagingRabbitmqApplication.toQueue;

@Component
@Slf4j
public class MyMessageListener implements javax.jms.MessageListener {
    @Autowired
    JmsTemplate jmsTemplate;
    @Autowired
    DefaultMessageListenerContainer container;

    @SneakyThrows
    @Override
    public void onMessage(Message message) {
        TextMessage txtMsg = (TextMessage) message;
        jmsTemplate.send(toQueue, new TextMessageCreator(txtMsg.getText()));
        counter.decrementAndGet();
        if (counter.get() % 50 == 0) {
            log.info(counter.get() + " left");
        }
        if (counter.get() < 1) {
            log.info("stopping container");
            container.stop();
        }
    }
}
