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

import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQTopic;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.jms.JMSException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BrokerTTLTest extends BaseTest {

    private BrokerService broker;

    @Before
    public void setup() throws Exception {
        broker = new BrokerService();
        broker.setPersistent(false);

        final TTLDestinationPlugin plugin = new TTLDestinationPlugin();
        plugin.setUseDefaultIfNoPolicy(false);

        final TTLDestinationMap map = new TTLDestinationMap();
        plugin.setMap(map);

        final TTLEntry destOneEntry = new TTLEntry();
        destOneEntry.setTtlCeiling(5000);
        destOneEntry.setZeroExpirationOverride(5000);
        map.put(new ActiveMQTopic("dest1"), destOneEntry);

        final TTLEntry destTwoEntry = new TTLEntry();
        destTwoEntry.setTtlCeiling(1000);
        destTwoEntry.setZeroExpirationOverride(1000);
        map.put(new ActiveMQTopic("dest2"), destTwoEntry);

        broker.setPlugins(new BrokerPlugin[] {plugin});
        broker.setUseJmx(true);
        broker.start();
    }

    public void tearDown() throws Exception {
        stopBroker(broker);
    }

    @Test
    public void testTTL() throws Exception {
        final AtomicInteger destOneReceived = new AtomicInteger(0);
        final AtomicInteger destTwoReceived = new AtomicInteger(0);

        final CountDownLatch latch = new CountDownLatch(30);

        listenOnTopic("vm://localhost", "dest1", message -> {
            try {
                final long jmsTimestamp = message.getJMSTimestamp();
                final long jmsExpiration = message.getJMSExpiration();

                Assert.assertEquals (5000, jmsExpiration - jmsTimestamp);
                destOneReceived.incrementAndGet();
            } catch (JMSException e) {
                Assert.fail(e.getMessage());
            }
        });

        listenOnTopic("vm://localhost", "dest2", message -> {
            try {
                final long jmsTimestamp = message.getJMSTimestamp();
                final long jmsExpiration = message.getJMSExpiration();

                Assert.assertEquals (1000, jmsExpiration - jmsTimestamp);
                destTwoReceived.incrementAndGet();
            } catch (JMSException e) {
                Assert.fail(e.getMessage());
            }
        });

        // send messages
        produceMessagesOnTopic("vm://localhost", "dest1", 1);
        produceMessagesOnTopic("vm://localhost", "dest2", 1);

        latch.await(30, TimeUnit.SECONDS);

        // these should all be received at the client
        Assert.assertEquals(1, destOneReceived.get());
        Assert.assertEquals(1, destTwoReceived.get());
    }
}
