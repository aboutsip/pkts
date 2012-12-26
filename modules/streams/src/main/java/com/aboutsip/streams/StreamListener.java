/**
 * 
 */
package com.aboutsip.streams;

import com.aboutsip.yajpcap.packet.Packet;

/**
 * Whenever a new {@link Packet} that belongs to a particular {@link Stream} has
 * been detected by the {@link StreamHandler}, it will call its registered
 * {@link StreamListener}s.
 * 
 * @author jonas@jonasborjesson.com
 * 
 */
public interface StreamListener<T extends Packet> {

    /**
     * Called when a new {@link Stream} is detected.
     * 
     * @param stream
     *            the new {@link Stream}.
     */
    void startStream(Stream<T> stream);

    /**
     * Called when a new {@link Packet} has been received.
     * 
     * @param stream
     *            the {@link Stream} to which the new {@link Packet} is
     *            associated with.
     * @param packet
     *            the new {@link Packet}.
     */
    void packetReceived(Stream<T> stream, T packet);

    /**
     * Called when the {@link Stream} has ended. There are many reasons why a
     * {@link Stream} ends and it depends on the type of {@link Stream}. For
     * some types of a {@link Stream}s, there is no "natural" end so
     * occasionally {@link Stream}s will be determined dead after some kind of
     * timeout. For other protocols, such as SIP, the {@link Stream} will end
     * when the dialog dies (if there is a dialog established in the first
     * place).
     * 
     * @param stream
     *            the {@link Stream} that just ended.
     */
    void endStream(Stream<T> stream);

}
