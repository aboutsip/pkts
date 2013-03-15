/**
 * 
 */
package com.aboutsip.yajpcap.packet;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.protocol.IllegalProtocolException;
import com.aboutsip.yajpcap.protocol.Protocol;

/**
 * 
 * @author jonas@jonasborjesson.com
 */
public interface TransportPacketFactory {

    /**
     * Create a new {@link TransportPacket}.
     * 
     * @param protocol
     *            which protocol, currently only {@link Protocol#UDP} and
     *            {@link Protocol#TCP} are supported
     * @param srcAddress
     *            the source address.
     * @param srcPort
     *            the source port
     * @param destAddress
     *            the destination address
     * @param destPort
     *            the destination port
     * @param payload
     *            the payload or null if none
     * @return a newly created {@link TransportPacket}
     * @throws IllegalArgumentException
     * @throws {@link IllegalProtocolException} in case any other protocol but
     *         {@link Protocol#UDP} or {@link Protocol#TCP} was specified.
     */
    TransportPacket create(Protocol protocol, String srcAddress, int srcPort, String destAddress, int destPort,
            Buffer payload) throws IllegalArgumentException, IllegalProtocolException;

}
