# Per Destination TTL Plugin

This plugin essentially uses the same logic as the `TimeStampingBrokerPlugin` built into ActiveMQ,
but provides the functionality on a per-destination basis.

You can provide configuration both for the default TTL times, and also provide specific entries for
specific destinations. Much like Policy Entries, or Destination Entries in the Authorization plugin,
destinations can be referred to using `>` and `*` wildcards.

A simple example config is shown below. This config applies a default, and maximum TTL of 12 hours,
but overrides this to provide a specific default and maximum TTL of 72 hours for any topic that 
starts with the name `72h.`.

````
        <plugins>
            <bean xmlns="http://www.springframework.org/schema/beans" class="org.tomitribe.perdestttl.TTLDestinationPlugin">
                <property name="ttlCeiling" value="43200000"/>
                <property name="zeroExpirationOverride" value="43200000"/>
                <property name="map">
                    <bean xmlns="http://www.springframework.org/schema/beans" class="org.tomitribe.perdestttl.TTLDestinationMap">
                        <property name="ttlEntries">
                            <list>
                                <bean xmlns="http://www.springframework.org/schema/beans" class="org.tomitribe.perdestttl.TTLEntry">
                                    <property name="topic" value="72h.>"/>
                                    <property name="ttlCeiling" value="259200000"/>
                                    <property name="zeroExpirationOverride" value="259200000"/>
                                </bean>
                            </list>
                        </property>
                    </bean>
                </property>
            </bean>
        </plugins>
````