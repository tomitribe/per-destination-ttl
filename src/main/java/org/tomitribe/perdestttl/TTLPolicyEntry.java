package org.tomitribe.perdestttl;

import org.apache.activemq.broker.region.policy.PolicyEntry;

public class TTLPolicyEntry extends PolicyEntry {

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
}
