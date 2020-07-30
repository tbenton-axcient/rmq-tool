package com.cloudfinder.rmqtool;

import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

public class TextMessageCreator implements MessageCreator {

    private final String message;

    /**
     * Create message created for text
     *
     * @param message
     */
    public TextMessageCreator(String message) {
        this.message = message;
    }

    @Override
    public Message createMessage(Session session) throws JMSException {
        return session.createTextMessage(message);
    }
}
