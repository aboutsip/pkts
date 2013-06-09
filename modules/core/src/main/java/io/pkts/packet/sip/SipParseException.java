/**
 * 
 */
package io.pkts.packet.sip;

import io.pkts.packet.PacketParseException;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipParseException extends PacketParseException {

    private static final long serialVersionUID = 7627471115511100108L;

    public SipParseException(final int errorOffset, final String message) {
        super(errorOffset, message);
    }

    public SipParseException(final int errorOffset, final String message, final Exception cause) {
        super(errorOffset, message, cause);
    }

}
