/**
 * 
 */
package io.pkts.streams.impl;

import io.pkts.packet.Packet;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Simple comparator of {@link Packet}s that is just comparing time stamps.
 * 
 * @author jonas@jonasborjesson.com
 */
public final class PacketComparator implements Comparator<Packet>, Serializable {

    /**
     * Because it is serializable. And the reason it is is because if you put elements in a treemap
     * or whatever then it cannot serialize itself unless the comparator is also serializable...
     */
    private static final long serialVersionUID = 1L;

    @Override
    public int compare(final Packet o1, final Packet o2) {
        final long t1 = o1.getArrivalTime();
        final long t2 = o2.getArrivalTime();
        if (t1 == t2) {
            return 0;
        }
        if (t1 < t2) {
            return -1;
        }
        return 1;
    }

}
