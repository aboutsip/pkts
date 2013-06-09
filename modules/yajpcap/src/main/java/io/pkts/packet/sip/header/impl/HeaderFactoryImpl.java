/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.header.HeaderFactory;
import io.pkts.packet.sip.header.ViaHeader;

import java.util.ArrayList;
import java.util.List;


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
        final List<Buffer[]> params = new ArrayList<Buffer[]>();
        return new ViaHeaderImpl(null, transport, host, Buffers.wrap(port), params);
    }

}
