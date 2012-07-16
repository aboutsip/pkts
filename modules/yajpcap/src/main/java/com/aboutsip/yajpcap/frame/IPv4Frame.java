/**
 * 
 */
package com.aboutsip.yajpcap.frame;

import java.io.IOException;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.framer.Framer;
import com.aboutsip.yajpcap.framer.FramerManager;
import com.aboutsip.yajpcap.protocol.Protocol;

/**
 * An IP Frame
 * 
 * @author jonas@jonasborjesson.com
 * 
 */
public final class IPv4Frame extends AbstractFrame {

    /**
     * Internet Header Length (IHL), which is the number of 32-bit words in the
     * header.
     */
    private final int length;

    /**
     * All the headers of this ip packet
     */
    private final Buffer headers;

    /**
     * If the {@code #length} is greater than 5, then this header carries some
     * extra options
     */
    private final int options;

    /**
     * The protocol contained in this ip packet. I.e., the
     */
    private final Protocol protocol;

    /**
     * 
     * @param length the header length
     * @param headers all the ipv4 headers in a buffer.
     * @param options if header length > 5, then we have a set of options as
     *            well
     * @param data the payload of the ip4v frame
     */
    public IPv4Frame(final FramerManager framerManager, final int length, final Buffer headers, final int options,
            final Buffer payload) throws IOException {
        super(framerManager, Protocol.IPv4, payload);
        this.length = length;
        this.headers = headers;
        this.options = options;

        // the protocol is in byte 10
        final byte code = headers.getByte(9);
        this.protocol = Protocol.valueOf(code);
    }

    /**
     * Check out http://en.wikipedia.org/wiki/IPv4 for a good explanation of the
     * IPv4 header frame
     * 
     * @param payload the total payload of the previous frame, which contains
     *            the ipv4 headers and its payload
     * @return
     */
    // public static IPv4Frame frame(final Buffer payload) {

    // }

    /**
     * Get the raw source ip.
     * 
     * Note, these are the raw bits and should be treated as such. If you really
     * want to print it, then you should treat it as unsigned
     * 
     * @return
     */
    public int getRawSourceIp() {
        return this.headers.getInt(12);
    }

    public String getSourceIp() {
        final short a = this.headers.getUnsignedByte(12);
        final short b = this.headers.getUnsignedByte(13);
        final short c = this.headers.getUnsignedByte(14);
        final short d = this.headers.getUnsignedByte(15);
        return a + "." + b + "." + c + "." + d;
    }

    /**
     * Get the raw destination ip.
     * 
     * Note, these are the raw bits and should be treated as such. If you really
     * want to print it, then you should treat it as unsigned
     * 
     * @return
     */
    public int getRawDestinationIp() {
        return this.headers.getInt(16);
    }

    public String getDestinationIp() {
        final short a = this.headers.getUnsignedByte(16);
        final short b = this.headers.getUnsignedByte(17);
        final short c = this.headers.getUnsignedByte(18);
        final short d = this.headers.getUnsignedByte(19);
        return a + "." + b + "." + c + "." + d;
    }

    /**
     * The version of this ip frame, will always be 4
     * 
     * @return
     */
    public int getVersion() {
        return 4;
    }

    /**
     * The length of the ipv4 headers
     * 
     * @return
     */
    public int getLength() {
        return this.length;
    }

    @Override
    protected Frame framePayload(final FramerManager framerManager, final Buffer payload) throws IOException {
        if (this.protocol == Protocol.UDP) {
            final Framer framer = framerManager.getFramer(Protocol.UDP);
            return framer.frame(payload);
        }
        return null;
    }

}
