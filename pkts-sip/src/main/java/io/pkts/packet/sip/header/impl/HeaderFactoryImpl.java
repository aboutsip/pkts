/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.address.Address;
import io.pkts.packet.sip.header.FromHeader;
import io.pkts.packet.sip.header.HeaderFactory;
import io.pkts.packet.sip.header.ToHeader;
import io.pkts.packet.sip.header.ViaHeader;

/**
 * @author jonas@jonasborjesson.com
 */
public final class HeaderFactoryImpl implements HeaderFactory {

    /**
     * 
     */
    public HeaderFactoryImpl() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public ViaHeader createViaHeader(final Buffer host, final int port, final Buffer transport, final Buffer branch) {
        return new ViaHeaderImpl(transport, host, port, branch);
    }

    @Override
    public ViaHeader createViaHeader(final String host, final int port, final String transport, final String branch) {
        return createViaHeader(Buffers.wrap(host), port, Buffers.wrap(transport), Buffers.wrap(branch));
    }

    @Override
    public ViaHeader createViaHeader(final String host, final int port, final String transport) {
        return createViaHeader(Buffers.wrap(host), port, Buffers.wrap(transport), null);
    }

    @Override
    public FromHeader createFromHeader(final Address from, final Buffer tag) {
        return new FromHeaderImpl(from, checkTag(tag));
    }

    @Override
    public ToHeader createToHeader(final Address to, final Buffer tag) {
        return new ToHeaderImpl(to, checkTag(tag));
    }

    private Buffer checkTag(final Buffer tag) {
        if (tag != null && tag.capacity() > 0) {
            return Buffers.wrap("tag=" + tag.toString());
        }

        return null;
    }

    @Override
    public FromHeader createFromHeader(final Buffer buffer) throws SipParseException {
        return FromHeaderImpl.frame(buffer);
    }

    @Override
    public ToHeader createToHeader(final Buffer buffer) throws SipParseException {
        return ToHeaderImpl.frame(buffer);
    }

}
