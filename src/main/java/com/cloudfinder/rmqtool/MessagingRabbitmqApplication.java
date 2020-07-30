package com.cloudfinder.rmqtool;

import com.rabbitmq.client.Address;
import com.rabbitmq.jms.admin.RMQConnectionFactory;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

@SpringBootApplication
public class MessagingRabbitmqApplication {
	static final String fromQueue = "o365MailTasks";
	static final String toQueue = "o365SharepointTasks";
	static final AtomicInteger numOfMessagesToMove = new AtomicInteger(200);

	private String username = "prd-tasks";
	private String password = "ahTh3ahr";
	private String hosts = "cf-prd-tasks-rmq1";

	private static class AddressListConnectionFactory extends RMQConnectionFactory {
		private static final long serialVersionUID = 6133967487035462030L;
		private final List<Address> addresses;

		public AddressListConnectionFactory(Address[] addresses) {
			this(Arrays.asList(addresses));
		}

		public AddressListConnectionFactory(List<Address> addresses) {
			this.addresses = addresses;
		}

		@Override
		public Connection createConnection() throws JMSException {
			return super.createConnection(addresses);
		}

		@Override
		public Connection createConnection(String username, String password) throws JMSException {
			return super.createConnection(username, password, addresses);
		}

		@Override
		public Connection createConnection(String username, String password,
										   List<Address> endpoints) throws JMSException {
			return super.createConnection(username, password, endpoints);
		}
	}

	private String username() {
		return username;
	}

	private String password() {
		return password;
	}

	private Address[] addresses() {
		return Address.parseAddresses(envAddressList());
	}

	private String envAddressList() {
		return hosts;
	}

	@Bean
	JmsTemplate jmsTemplate(@Qualifier("rabbitmqProducerConnectionFactory") ConnectionFactory connectionFactory) {
		return new JmsTemplate(connectionFactory);
	}

	@Bean(name = "rabbitmqConsumerConnectionFactory")
	public ConnectionFactory rabbitmqConsumerConnectionFactory() {
		final ConnectionFactory cf = createConnectionFactory();
		return cf;
	}

	@Bean(name = "rabbitmqProducerConnectionFactory")
	public ConnectionFactory rabbitmqProducerConnectionFactory() {
		final ConnectionFactory cf = createConnectionFactory();
		return cf;
	}

	ConnectionFactory createConnectionFactory() {
		RMQConnectionFactory factory = new AddressListConnectionFactory(addresses());
		factory.setUsername(username());
		factory.setPassword(password());
		// It's possible to use below setting to automatically requeue a message once a RuntimeException is thrown
		// However, TaskBrokerServerMessageListener.onMessage and WorkerMessageListener.onMessage
		// handle all exceptions explicitly, so this is not needed.
		// factory.setRequeueOnMessageListenerException(true);
		factory.setOnMessageTimeoutMs(Integer.MAX_VALUE);
		factory.setChannelsQos(1);
		// ^^ stop receiving messages until we are not acknowledged previous ones
		// the same as for active mq
		return new org.springframework.jms.connection.CachingConnectionFactory(factory);
	}

	@Bean
	Queue queue() {
		return new Queue(fromQueue, false);
	}

	@Bean
	DefaultMessageListenerContainer container(@Qualifier("rabbitmqConsumerConnectionFactory") ConnectionFactory connectionFactory,
											  MyMessageListener messageListener) {
		DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setMessageListener(messageListener);
		container.setDestinationName(fromQueue);
		container.setSessionTransacted(true);
		container.setConcurrency("1");
		container.setErrorHandler(new LoggingErrorHandler());
		container.setAutoStartup(true);
		return container;
	}

	public static void main(String[] args) {
		SpringApplication.run(MessagingRabbitmqApplication.class, args);
	}
}
