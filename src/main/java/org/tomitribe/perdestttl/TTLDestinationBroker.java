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
import org.apache.activemq.broker.BrokerFilter;
import org.apache.activemq.broker.region.CompositeDestinationInterceptor;
import org.apache.activemq.broker.region.DestinationInterceptor;
import org.apache.activemq.broker.region.RegionBroker;

import java.util.Arrays;

public class TTLDestinationBroker extends BrokerFilter {

    public TTLDestinationBroker(final Broker next,
                                final TTLDestinationMap map,
                                final long zeroExpirationOverride,
                                final long ttlCeiling,
                                final boolean futureOnly,
                                final boolean processNetworkMessages,
                                final boolean useDefaultIfNoPolicy) {
        super(next);

        // add DestinationInterceptor
        final RegionBroker regionBroker = (RegionBroker) next.getAdaptor(RegionBroker.class);
        final CompositeDestinationInterceptor compositeInterceptor = (CompositeDestinationInterceptor) regionBroker.getDestinationInterceptor();
        DestinationInterceptor[] interceptors = compositeInterceptor.getInterceptors();
        interceptors = Arrays.copyOf(interceptors, interceptors.length + 1);

        final TTLDestinationInterceptor interceptor =
                new TTLDestinationInterceptor(
                         this,
                                map,
                                zeroExpirationOverride,
                                ttlCeiling,
                                futureOnly,
                                processNetworkMessages,
                                useDefaultIfNoPolicy);

        interceptors[interceptors.length - 1] = interceptor;
        compositeInterceptor.setInterceptors(interceptors);
    }
}
