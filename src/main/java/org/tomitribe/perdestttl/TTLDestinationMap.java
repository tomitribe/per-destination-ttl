package org.tomitribe.perdestttl;

import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.filter.DestinationMap;
import org.apache.activemq.filter.DestinationMapEntry;

import java.util.List;

public class TTLDestinationMap extends DestinationMap {

    public void setTtlEntries(List<DestinationMapEntry> ttlEntries) {
        super.setEntries(ttlEntries);
    }

    public TTLEntry findEntry(final ActiveMQDestination destination) {
        final DestinationMapEntry destinationMapEntry = super.chooseValue(destination);
        if (destinationMapEntry == null) {
            return null;
        }

        if (! (destinationMapEntry instanceof TTLEntry)) {
            return null;
        }

        return (TTLEntry) destinationMapEntry;
    }
}
