/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.address.Address;
import io.pkts.packet.sip.header.FromHeader;
import io.pkts.packet.sip.header.HeaderFactory;
import io.pkts.packet.sip.header.ToHeader;

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

}
