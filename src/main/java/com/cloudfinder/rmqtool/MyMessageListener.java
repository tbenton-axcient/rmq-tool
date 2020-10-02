package com.cloudfinder.rmqtool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.Message;
import javax.jms.TextMessage;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static com.cloudfinder.rmqtool.MessagingRabbitmqApplication.getToQueue;

@Component
@Slf4j
public class MyMessageListener implements javax.jms.MessageListener {
    @Autowired
    JmsTemplate jmsTemplate;
    @Autowired
    DefaultMessageListenerContainer container;
    AtomicInteger numOfMessagesToMove = new AtomicInteger(5000);

    @SneakyThrows
    @Override
    public void onMessage(Message message) {
        TextMessage txtMsg = (TextMessage) message;
        String queueName = getToQueue();
        log.info("Sending a message to {}", queueName);
        jmsTemplate.send(queueName, new TextMessageCreator(txtMsg.getText()));
        numOfMessagesToMove.decrementAndGet();
        if (numOfMessagesToMove.get() % 10 == 0) {
            log.info(numOfMessagesToMove.get() + " left");
        }
        if (numOfMessagesToMove.get() < 1) {
            log.info("stopping container");
            container.stop();
        }
    }
}
