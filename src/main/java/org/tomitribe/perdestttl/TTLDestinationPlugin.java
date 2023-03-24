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
import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.security.AuthorizationMap;

public class TTLDestinationPlugin implements BrokerPlugin {
    /**
     * variable which (when non-zero) is used to override
     * the expiration date for messages that arrive with
     * no expiration date set (in Milliseconds).
     */
    protected long zeroExpirationOverride = 0;

    /**
     * variable which (when non-zero) is used to limit
     * the expiration date (in Milliseconds).
     */
    protected long ttlCeiling = 0;

    /**
     * If true, the plugin will not update timestamp to past values
     * False by default
     */
    protected boolean futureOnly = false;


    /**
     * if true, update timestamp even if message has passed through a network
     * default false
     */
    protected boolean processNetworkMessages = false;

    /**
     * if true, apply the timestamp using the defaults specified on this interceptor if there is no matching policy
     * if false, and there is no matching policy, bypass the logic in this interceptor
     */
    protected boolean useDefaultIfNoPolicy = true;
    private TTLDestinationMap map;


    @Override
    public Broker installPlugin(Broker broker) throws Exception {

        return new TTLDestinationBroker(
                broker,
                map,
                zeroExpirationOverride,
                ttlCeiling,
                futureOnly,
                processNetworkMessages,
                useDefaultIfNoPolicy);
    }

    public long getZeroExpirationOverride() {
        return zeroExpirationOverride;
    }

    public void setZeroExpirationOverride(long zeroExpirationOverride) {
        this.zeroExpirationOverride = zeroExpirationOverride;
    }

    public long getTtlCeiling() {
        return ttlCeiling;
    }

    public void setTtlCeiling(long ttlCeiling) {
        this.ttlCeiling = ttlCeiling;
    }

    public boolean isFutureOnly() {
        return futureOnly;
    }

    public void setFutureOnly(boolean futureOnly) {
        this.futureOnly = futureOnly;
    }

    public boolean isProcessNetworkMessages() {
        return processNetworkMessages;
    }

    public void setProcessNetworkMessages(boolean processNetworkMessages) {
        this.processNetworkMessages = processNetworkMessages;
    }

    public boolean isUseDefaultIfNoPolicy() {
        return useDefaultIfNoPolicy;
    }

    public void setUseDefaultIfNoPolicy(boolean useDefaultIfNoPolicy) {
        this.useDefaultIfNoPolicy = useDefaultIfNoPolicy;
    }

    public TTLDestinationMap getMap() {
        return map;
    }

    public void setMap(TTLDestinationMap map) {
        this.map = map;
    }

}
