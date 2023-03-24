package org.tomitribe.perdestttl;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerPlugin;

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

    @Override
    public Broker installPlugin(Broker broker) throws Exception {

        return new TTLDestinationBroker(
                broker,
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
}
