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

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.broker.ProducerBrokerExchange;
import org.apache.activemq.broker.region.Destination;
import org.apache.activemq.broker.region.DestinationFilter;
import org.apache.activemq.broker.region.DestinationInterceptor;
import org.apache.activemq.broker.region.policy.DeadLetterStrategy;
import org.apache.activemq.broker.util.TimeStampingBrokerPlugin;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class TTLDestinationInterceptor implements DestinationInterceptor {

    public TTLDestinationInterceptor(final Broker broker,
                                     final TTLDestinationMap map,
                                     final long zeroExpirationOverride,
                                     final long ttlCeiling,
                                     final boolean futureOnly,
                                     final boolean processNetworkMessages,
                                     final boolean useDefaultIfNoPolicy) {
        this.broker = broker;
        this.map = map;
        this.zeroExpirationOverride = zeroExpirationOverride;
        this.ttlCeiling = ttlCeiling;
        this.futureOnly = futureOnly;
        this.processNetworkMessages = processNetworkMessages;
        this.useDefaultIfNoPolicy = useDefaultIfNoPolicy;
    }

    private static final Logger LOG = LoggerFactory.getLogger(TimeStampingBrokerPlugin.class);

    protected final Broker broker;

    private final TTLDestinationMap map;
    /**
     * variable which (when non-zero) is used to override
     * the expiration date for messages that arrive with
     * no expiration date set (in Milliseconds).
     */
    protected final long zeroExpirationOverride;

    /**
     * variable which (when non-zero) is used to limit
     * the expiration date (in Milliseconds).
     */
    protected final long ttlCeiling;

    /**
     * If true, the plugin will not update timestamp to past values
     * False by default
     */
    protected final boolean futureOnly;


    /**
     * if true, update timestamp even if message has passed through a network
     * default false
     */
    protected final boolean processNetworkMessages;

    /**
     * if true, apply the timestamp using the defaults specified on this interceptor if there is no matching policy
     * if false, and there is no matching policy, bypass the logic in this interceptor
     */
    protected final boolean useDefaultIfNoPolicy;

    @Override
    public Destination intercept(final Destination destination) {
        final ActiveMQDestination activeMQDestination = destination.getActiveMQDestination();
        final TTLEntry entry = map.findEntry(activeMQDestination);

        if (entry == null) {
            if (useDefaultIfNoPolicy) {
                return new TimestampingDestinationFilter(
                        destination,
                        processNetworkMessages,
                        zeroExpirationOverride,
                        ttlCeiling,
                        futureOnly);

            } else {
                return destination;
            }
        }

        return new TimestampingDestinationFilter(
                destination,
                entry.isProcessNetworkMessages(),
                entry.getZeroExpirationOverride(),
                entry.getTtlCeiling(),
                entry.isFutureOnly());
    }

    @Override
    public void remove(Destination destination) {

    }

    @Override
    public void create(final Broker broker, final ConnectionContext context, final ActiveMQDestination amqDest) throws Exception {
    }

    private static class TimestampingDestinationFilter extends DestinationFilter {

        private final boolean processNetworkMessages;
        private final long zeroExpirationOverride;
        private final long ttlCeiling;
        private final boolean futureOnly;

        public TimestampingDestinationFilter(Destination destination, boolean processNetworkMessages, long zeroExpirationOverride, long ttlCeiling, boolean futureOnly) {
            super(destination);
            this.processNetworkMessages = processNetworkMessages;
            this.zeroExpirationOverride = zeroExpirationOverride;
            this.ttlCeiling = ttlCeiling;
            this.futureOnly = futureOnly;
        }

        public void send(ProducerBrokerExchange context, Message message) throws Exception {
            if (message.getTimestamp() > 0 && !isDestinationDLQ(message) &&
                    (processNetworkMessages || (message.getBrokerPath() == null || message.getBrokerPath().length == 0))) {
                // timestamp not been disabled and has not passed through a network or processNetworkMessages=true

                long oldExpiration = message.getExpiration();
                long newTimeStamp = System.currentTimeMillis();
                long timeToLive = zeroExpirationOverride;
                long oldTimestamp = message.getTimestamp();
                if (oldExpiration > 0) {
                    timeToLive = oldExpiration - oldTimestamp;
                }
                if (timeToLive > 0 && ttlCeiling > 0 && timeToLive > ttlCeiling) {
                    timeToLive = ttlCeiling;
                }
                long expiration = timeToLive + newTimeStamp;
                // In the scenario that the Broker is behind the clients we never want to set the
                // Timestamp and Expiration in the past
                if(!futureOnly || (expiration > oldExpiration)) {
                    if (timeToLive > 0 && expiration > 0) {
                        message.setExpiration(expiration);
                    }
                    message.setTimestamp(newTimeStamp);
                    LOG.debug("Set message {} timestamp from {} to {}",
                            message.getMessageId(), oldTimestamp, newTimeStamp);
                }
            }
            super.send(context, message);
        }

        private boolean isDestinationDLQ(Message message) {
            DeadLetterStrategy deadLetterStrategy;
            Message tmp;

            Destination regionDestination = (Destination) message.getRegionDestination();
            if (message != null && regionDestination != null) {
                deadLetterStrategy = regionDestination.getDeadLetterStrategy();
                if (deadLetterStrategy != null && message.getOriginalDestination() != null) {
                    // Cheap copy, since we only need two fields
                    tmp = new ActiveMQMessage();
                    tmp.setDestination(message.getOriginalDestination());
                    tmp.setRegionDestination(regionDestination);

                    // Determine if we are headed for a DLQ
                    ActiveMQDestination deadLetterDestination = deadLetterStrategy.getDeadLetterQueueFor(tmp, null);
                    if (deadLetterDestination.equals(message.getDestination())) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
