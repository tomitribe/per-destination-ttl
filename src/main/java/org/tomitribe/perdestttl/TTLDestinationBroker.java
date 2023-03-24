package org.tomitribe.perdestttl;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerFilter;
import org.apache.activemq.broker.region.CompositeDestinationInterceptor;
import org.apache.activemq.broker.region.DestinationInterceptor;
import org.apache.activemq.broker.region.RegionBroker;

import java.util.Arrays;

public class TTLDestinationBroker extends BrokerFilter {

    public TTLDestinationBroker(final Broker next,
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
                                zeroExpirationOverride,
                                ttlCeiling,
                                futureOnly,
                                processNetworkMessages,
                                useDefaultIfNoPolicy);

        interceptors[interceptors.length - 1] = interceptor;
        compositeInterceptor.setInterceptors(interceptors);
    }
}
