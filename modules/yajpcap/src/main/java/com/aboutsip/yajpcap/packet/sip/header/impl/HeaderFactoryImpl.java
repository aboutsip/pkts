/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.header.impl;

import java.util.ArrayList;
import java.util.List;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.packet.sip.header.HeaderFactory;
import com.aboutsip.yajpcap.packet.sip.header.ViaHeader;

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
