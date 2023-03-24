/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.perdestttl;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.junit.Assert;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class BaseTest {
    protected void cancelSubscription(final String brokerUrl, final String clientName) throws Exception {
        final ConnectionFactory cf = new ActiveMQConnectionFactory(brokerUrl);
        final Connection conn = cf.createConnection();
        conn.setClientID(clientName);
        conn.start();

        final Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        session.unsubscribe(clientName);
        session.close();
        conn.stop();
        conn.close();
    }

    protected MessageConsumer createDurableSubscriber(final String brokerUrl, final String topicName, final String clientName) throws Exception {
        final ConnectionFactory cf = new ActiveMQConnectionFactory(brokerUrl);
        final Connection conn = cf.createConnection();
        conn.setClientID(clientName);
        conn.start();

        final Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        final Topic topic = session.createTopic(topicName);
        final TopicSubscriber subscriber = session.createDurableSubscriber(topic, clientName);

        return new MessageConsumerWrapper(subscriber, session, conn);
    }

    protected void consumeMessagesFromQueue(final String brokerUrl, final String queueName, final int numberOfMessages) throws Exception {
        final ConnectionFactory cf = new ActiveMQConnectionFactory(brokerUrl);
        final Connection conn = cf.createConnection();
        conn.start();

        final Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

        final Queue queue = session.createQueue(queueName);
        final MessageConsumer consumer = session.createConsumer(queue);

        for (int i = 0; i < numberOfMessages; i++) {
            final Message receivedMessage = consumer.receive(1000);
            Assert.assertNotNull(receivedMessage);
        }

        consumer.close();
        session.close();
        conn.close();
    }

    protected Message getNextMessageFromQueue(final String brokerUrl, final String queueName) throws Exception {
        final ConnectionFactory cf = new ActiveMQConnectionFactory(brokerUrl);
        final Connection conn = cf.createConnection();
        conn.start();

        final Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

        final Queue queue = session.createQueue(queueName);
        final MessageConsumer consumer = session.createConsumer(queue);

        final Message receivedMessage = consumer.receive(1000);

        consumer.close();
        session.close();
        conn.close();

        return receivedMessage;
    }

    protected void produceMessagesOnQueue(final String brokerUrl, final String queueName, final int numberOfMessages) throws Exception {
        produceMessagesOnQueue(brokerUrl, queueName, numberOfMessages, 0);
    }

    protected void produceMessagesOnQueue(final String brokerUrl, final String queueName, final int numberOfMessages, int messageSize) throws Exception {
        final ConnectionFactory cf = new ActiveMQConnectionFactory(brokerUrl);
        final Connection conn = cf.createConnection();
        conn.start();

        final Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

        final Queue queue = session.createQueue(queueName);
        sendMessagesToDestination(numberOfMessages, messageSize, session, queue);
        session.close();
        conn.close();
    }

    protected void produceMessagesOnTopic(final String brokerUrl, final String topicName, final int numberOfMessages) throws Exception {
        produceMessagesOnTopic(brokerUrl, topicName, numberOfMessages, 0);
    }

    private void produceMessagesOnTopic(final String brokerUrl, final String topicName, final int numberOfMessages, int messageSize) throws Exception {
        final ConnectionFactory cf = new ActiveMQConnectionFactory(brokerUrl);
        final Connection conn = cf.createConnection();
        conn.start();

        final Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

        final Topic topic = session.createTopic(topicName);
        sendMessagesToDestination(numberOfMessages, messageSize, session, topic);
        session.close();
        conn.close();
    }

    private void sendMessagesToDestination(final int numberOfMessages, final int messageSize, final Session session, final Destination dest) throws JMSException, IOException {
        final MessageProducer producer = session.createProducer(dest);

        for (int i = 0; i < numberOfMessages; i++) {
            final String messageText;
            if (messageSize < 1) {
                messageText = "Test message: " + i;
            } else {
                messageText = readInputStream(getClass().getResourceAsStream("demo.txt"), messageSize, i);
            }

            final TextMessage message = session.createTextMessage(messageText);
            message.setIntProperty("messageno", i);
            producer.send(message);
        }

        producer.close();
    }

    private String readInputStream(InputStream is, int size, int messageNumber) throws IOException {
        InputStreamReader reader = new InputStreamReader(is);
        try {
            char[] buffer;
            if (size > 0) {
                buffer = new char[size];
            } else {
                buffer = new char[1024];
            }
            int count;
            StringBuilder builder = new StringBuilder();
            while ((count = reader.read(buffer)) != -1) {
                builder.append(buffer, 0, count);
                if (size > 0) break;
            }
            return builder.toString();
        } finally {
            reader.close();
        }
    }

    protected void stopBroker(BrokerService broker) throws Exception {
        broker.stop();
    }

    protected MessageConsumer listenOnQueue(final String brokerUri, final String queueName, final MessageListener messageListener) throws Exception {
        final ConnectionFactory cf = new ActiveMQConnectionFactory(brokerUri);
        final Connection conn = cf.createConnection();
        conn.start();

        final Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

        final Queue queue = session.createQueue(queueName);
        final MessageConsumer consumer = session.createConsumer(queue);
        consumer.setMessageListener(messageListener);

        return new MessageConsumerWrapper(consumer, session, conn);
    }

    protected MessageConsumer listenOnTopic(final String brokerUri, final String topicName, final MessageListener messageListener) throws Exception {
        final ConnectionFactory cf = new ActiveMQConnectionFactory(brokerUri);
        final Connection conn = cf.createConnection();
        conn.start();

        final Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

        final Topic topic = session.createTopic(topicName);
        final MessageConsumer consumer = session.createConsumer(topic);
        consumer.setMessageListener(messageListener);

        return new MessageConsumerWrapper(consumer, session, conn);
    }

    private static class MessageConsumerWrapper implements MessageConsumer {
        private final MessageConsumer messageConsumer;
        private final Session session;
        private final Connection conn;

        public MessageConsumerWrapper(final MessageConsumer messageConsumer, final Session session, final Connection conn) {
            this.messageConsumer = messageConsumer;
            this.session = session;
            this.conn = conn;
        }

        @Override
        public String getMessageSelector() throws JMSException {
            return messageConsumer.getMessageSelector();
        }

        @Override
        public MessageListener getMessageListener() throws JMSException {
            return messageConsumer.getMessageListener();
        }

        @Override
        public void setMessageListener(final MessageListener messageListener) throws JMSException {
            messageConsumer.setMessageListener(messageListener);
        }

        @Override
        public Message receive() throws JMSException {
            return messageConsumer.receive();
        }

        @Override
        public Message receive(final long timeout) throws JMSException {
            return messageConsumer.receive(timeout);
        }

        @Override
        public Message receiveNoWait() throws JMSException {
            return messageConsumer.receiveNoWait();
        }

        @Override
        public void close() throws JMSException {
            messageConsumer.close();
            session.close();
            conn.stop();
            conn.close();
        }
    }
}
