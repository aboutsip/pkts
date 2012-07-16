/**
 * 
 */
package com.aboutsip.yajpcap.framer;

import java.io.IOException;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.frame.EthernetFrame;
import com.aboutsip.yajpcap.frame.Frame;
import com.aboutsip.yajpcap.frame.UnknownEtherType;
import com.aboutsip.yajpcap.protocol.Protocol;

/**
 * Simple framer for framing Ethernet frames
 * 
 * @author jonas@jonasborjesson.com
 */
public class EthernetFramer implements Framer {

    private final FramerManager framerManager;

    public EthernetFramer(final FramerManager framerManager) {
        this.framerManager = framerManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Protocol getProtocol() {
        return Protocol.ETHERNET_II;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Frame frame(final Buffer buffer) throws IOException {
        final Buffer destMacAddress = buffer.readBytes(6);
        final Buffer srcMacAddress = buffer.readBytes(6);
        final byte b1 = buffer.readByte();
        final byte b2 = buffer.readByte();

        EthernetFrame.EtherType etherType;
        try {
            etherType = getEtherType(b1, b2);
        } catch (final UnknownEtherType e) {
            throw new RuntimeException("uknown ether type");
        }

        final Buffer data = buffer.slice(buffer.capacity());

        return new EthernetFrame(this.framerManager, destMacAddress, srcMacAddress, etherType, data);
    }

    private static EthernetFrame.EtherType getEtherType(final byte b1, final byte b2) throws UnknownEtherType {
        if ((b1 == (byte) 0x08) && (b2 == (byte) 0x00)) {
            return EthernetFrame.EtherType.IPv4;
        } else if ((b1 == (byte) 0x86) && (b2 == (byte) 0xdd)) {
            return EthernetFrame.EtherType.IPv6;
        }

        // will implement as we need to
        throw new UnknownEtherType(b1, b2);

    }

    @Override
    public boolean accept(final Buffer data) {
        // TODO Auto-generated method stub
        return false;
    }

}
