/**
 * 
 */
package com.aboutsip.streams;

import java.util.Iterator;

import com.aboutsip.yajpcap.packet.Packet;

/**
 * A {@link Stream} represents a set of {@link Packet}s that belong together.
 * E.g., in SIP, messages that goes within the same dialog will be grouped into
 * a single stream.
 * 
 * @author jonas@jonasborjesson.com
 */
public interface Stream<T extends Packet> {

    /**
     * Get all {@link Packet}s that belongs to this stream.
     * 
     * @return
     */
    Iterator<T> getPackets();

}
