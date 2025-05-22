package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.header.ExpiresHeader;

/** 
* ExpiresHeaderImpl is a specialized SIP header implementation for managing session expiration times in network communication. 
* It extends SipHeaderImpl and implements the ExpiresHeader interface, providing a concrete mechanism to store and retrieve expiration values for SIP sessions. 
* The class offers methods to create, access, and clone expiration headers, with a core focus on representing the integer-based expires time for SIP communication protocols. 
* It supports basic header manipulation by storing an expires value and allowing retrieval and duplication of that value, 
* facilitating session timeout and lifecycle management in SIP-based network interactions."
 */

public class ExpiresHeaderImpl extends SipHeaderImpl implements ExpiresHeader {

    private int expires;

    public ExpiresHeaderImpl(final int expires) {
        super(ExpiresHeader.NAME, Buffers.wrap(expires));
        this.expires = expires;
    }

    @Override
    public int getExpires() {
        return this.expires;
    }

    @Override
    public ExpiresHeader clone() {
        return new ExpiresHeaderImpl(this.expires);
    }

    @Override
    public ExpiresHeader ensure() {
        return this;
    }

    @Override
    public ExpiresHeader.Builder copy() {
        return new ExpiresHeader.Builder(this.expires);
    }
}
