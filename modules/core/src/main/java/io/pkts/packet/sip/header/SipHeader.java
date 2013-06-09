/**
 * 
 */
package io.pkts.packet.sip.header;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.SipParseException;


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

}
