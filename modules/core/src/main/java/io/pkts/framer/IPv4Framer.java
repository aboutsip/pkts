/**
 * 
 */
package io.pkts.framer;

import io.pkts.buffer.Buffer;
import io.pkts.frame.IPv4Frame;
import io.pkts.frame.Layer2Frame;
import io.pkts.protocol.Protocol;

import java.io.IOException;


/**
 * @author jonas@jonasborjesson.com
 * 
 */
public class IPv4Framer implements Layer3Framer {

    private final FramerManager framerManager;

    public IPv4Framer(final FramerManager framerManager) {
        this.framerManager = framerManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Protocol getProtocol() {
        return Protocol.IPv4;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPv4Frame frame(final Layer2Frame parent, final Buffer payload) throws IOException {

        if (parent == null) {
            throw new IllegalArgumentException("The parent frame cannot be null");
        }

        Layer2Frame parentFrame = null;
        try {
            parentFrame = parent;
        } catch (final ClassCastException e) {
            throw new IllegalArgumentException("The parent frame must be of type "
                    + Layer2Frame.class.getCanonicalName());
        }

        // the ipv4 headers are always 20 bytes unless
        // the length is greater than 5
        final Buffer headers = payload.readBytes(20);

        // byte 1, contains the version and the length
        final byte b = headers.getByte(0);
        // final int version = ((i >>> 28) & 0x0F);
        // final int length = ((i >>> 24) & 0x0F);

        final int version = b >>> 5 & 0x0F;
        final int length = b & 0x0F;

        // byte 2 - dscp and ecn
        // final byte b2 = headers.readByte();

        // final int dscp = ((b2 >>> 6) & 0x3B);
        // final int ecn = (b2 & 0x03);

        // byte 3 - 4
        // final int totalLength = headers.readUnsignedShort();

        // byte 5 - 6
        // final short id = headers.readShort();

        // this one contains flags + fragment offset

        // byte 7 - 8
        // final short flagsAndFragement = headers.readShort();

        // byte 9
        // final byte ttl = headers.readByte();

        // byte 10
        // final byte protocol = headers.getByte(9);

        // byte 11 - 12
        // final int checkSum = headers.readUnsignedShort();

        // byte 13 - 16
        // final int sourceIp = headers.readInt();

        // byte 17 - 20
        // final int destIp = headers.readInt();

        // System.out.println(version);
        // System.out.println(length);
        // System.out.println(dscp);
        // System.out.println(ecn);
        // System.out.println(totalLength);
        // System.out.println(id);
        // System.out.println(ttl);
        // System.out.println(protocol);
        // System.out.println(checkSum);
        // System.out.println(sourceIp);
        // System.out.println(destIp);

        // if the length is greater than 5, then the frame
        // contains extra options so read those as well
        int options = 0;
        if (length > 5) {
            // remember, this may have to be treated as unsigned
            // final int options = headers.readInt();
            options = payload.readInt();
        }

        final Buffer data = payload.slice();

        return new IPv4Frame(this.framerManager, parentFrame.getPcapGlobalHeader(), parentFrame, length, headers,
                options, data);
    }

    @Override
    public boolean accept(final Buffer data) {
        // TODO Auto-generated method stub
        return false;
    }

}
