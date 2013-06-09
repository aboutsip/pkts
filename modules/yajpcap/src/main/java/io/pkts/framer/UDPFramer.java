/**
 * 
 */
package io.pkts.framer;

import io.pkts.buffer.Buffer;
import io.pkts.frame.Layer3Frame;
import io.pkts.frame.UDPFrame;
import io.pkts.protocol.Protocol;

import java.io.IOException;


/**
 * @author jonas@jonasborjesson.com
 */
public class UDPFramer implements Layer4Framer {

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
    public UDPFrame frame(final Layer3Frame parent, final Buffer buffer) throws IOException {
        if (parent == null) {
            throw new IllegalArgumentException("The parent frame cannot be null");
        }

        // UDP header is very simple. For our purposes, just
        // read the 8 bytes containing all the header fields
        // and the rest is just user data (payload of the udp packet)
        final Buffer headers = buffer.readBytes(8);
        final Buffer data = buffer.slice();
        return new UDPFrame(this.framerManager, parent.getPcapGlobalHeader(), parent, headers, data);
    }

    @Override
    public boolean accept(final Buffer data) {
        // TODO Auto-generated method stub
        return false;
    }

}
