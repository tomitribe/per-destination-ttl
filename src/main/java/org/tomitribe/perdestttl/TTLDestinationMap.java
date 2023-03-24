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
