/**
 * 
 */
package io.pkts.packet.sip.header;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.impl.SipParser;


/**
 * Represents any header in SIP.
 * 
 * Note, by default most things are done lazily in order to speed things up. As
 * such, you may successfully construct a header but it may in fact miss
 * important information. If you are building an application where you want to
 * be 100% sure that a header is correct according to the BNF in rfc 3261 then
 * call {@link #verify()}.
 * 
 * @author jonas@jonasborjesson.com
 */
public interface SipHeader extends Cloneable {

    /**
     * Get the name of the header
     * 
     * @return
     */
    Buffer getName();

    /**
     * Get the value of the buffer
     * 
     * @return
     */
    Buffer getValue();

    void verify() throws SipParseException;

    void getBytes(Buffer dst);

    SipHeader clone();

    /**
     * Create a new {@link SipHeader} based on the buffer. Each {@link SipHeader} will override this
     * factory method to parse the header into a more specialized header.
     * 
     * Note, the header returned really is a {@link SipHeader} and is NOT e.g. a {@link ToHeader}.
     * If you really need to parse it as a {@link ToHeader} you should use the
     * 
     * @param header the raw header
     * @return a new {@link SipHeader}.
     * @throws SipParseException in case the header is not a correct formatted header.
     */
    static SipHeader create(final Buffer header) throws SipParseException {
        return SipParser.nextHeader(header);
    }

}
