/**
 * 
 */
package com.aboutsip.yajpcap.framer;

import java.io.IOException;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.frame.Frame;
import com.aboutsip.yajpcap.frame.UDPFrame;
import com.aboutsip.yajpcap.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 */
public class UDPFramer implements Framer {

    private final FramerManager framerManager;

    /**
     * 
     */
    public UDPFramer(final FramerManager framerManager) {
        this.framerManager = framerManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Protocol getProtocol() {
        return Protocol.UDP;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Frame frame(final Buffer buffer) throws IOException {
        // UDP header is very simple. For our purposes, just
        // read the 8 bytes containing all the header fields
        // and the rest is just user data (payload of the udp packet)
        final Buffer headers = buffer.readBytes(8);
        final Buffer data = buffer.slice();
        return new UDPFrame(this.framerManager, headers, data);
    }

    @Override
    public boolean accept(final Buffer data) {
        // TODO Auto-generated method stub
        return false;
    }

}
