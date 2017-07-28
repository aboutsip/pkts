/**
 * 
 */
package io.pkts.packet.sip;

import io.pkts.packet.PacketParseException;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipPacketParseException extends PacketParseException {

    private static final long serialVersionUID = 7627471115511100108L;

    public SipPacketParseException(final int errorOffset, final String message) {
        super(errorOffset, message);
    }

    public SipPacketParseException(final int errorOffset, final String message, final Exception cause) {
        super(errorOffset, message, cause);
    }

}
