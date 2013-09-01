/**
 * 
 */
package io.pkts.packet.sip.header;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.address.Address;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public interface HeaderFactory {

    /**
     * Create a {@link ViaHeader}.
     * 
     * @param host
     * @param port
     * @param transport
     * @param branch
     * @return
     */
    ViaHeader createViaHeader(Buffer host, int port, Buffer transport, Buffer branch);

    /**
     * Convenience method for creating a new {@link ViaHeader} based on
     * {@link String} instead of {@link Buffer}.
     * 
     * @param host
     * @param port
     * @param transport
     * @param branch
     * @return
     */
    ViaHeader createViaHeader(String host, int port, String transport, String branch);

    /**
     * Same as {@link #createViaHeader(String, int, String, String)} but the
     * branch-id gets generated.
     * 
     * @param host
     * @param port
     * @param transport
     * @return
     */
    ViaHeader createViaHeader(String host, int port, String transport);

    FromHeader createFromHeader(Address from, Buffer tag);

    /**
     * Will create a new {@link FromHeader} by parsing the supplied
     * {@link Buffer}.
     * 
     * @param buffer
     * @return
     */
    FromHeader createFromHeader(Buffer buffer) throws SipParseException;

    ToHeader createToHeader(Address to, Buffer tag);

    ToHeader createToHeader(Buffer buffer) throws SipParseException;

}
