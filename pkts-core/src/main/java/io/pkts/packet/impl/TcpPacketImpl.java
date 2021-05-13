/**
 * 
 */
package io.pkts.packet.impl;

import java.io.IOException;
import java.io.OutputStream;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.IPPacket;
import io.pkts.packet.TCPPacket;
import io.pkts.packet.TransportPacket;
import io.pkts.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public final class TcpPacketImpl extends TransportPacketImpl implements TCPPacket {

    private final Buffer headers;

    private final Buffer options;

    /**
     * @param parent
     * @param headers
     */
    public TcpPacketImpl(final IPPacket parent, final Buffer headers, final Buffer options, final Buffer payload) {
        super(parent, Protocol.TCP, headers, payload);
        this.headers = headers;
        this.options = options;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTCP() {
        return true;
    }

    @Override
    public int getChecksum() {
        return this.headers.getUnsignedShort(16);
    }

    @Override
    public int getUrgentPointer() {
        return this.headers.getUnsignedShort(18);
    }

    @Override
    public int getWindowSize() {
        return this.headers.getUnsignedShort(14);
    }

    @Override
    public short getReserved() {
        try {
            final byte a = this.headers.getByte(12);
            return (short) ((a >> 1) & 0x7);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the header length in bytes
     * 
     * @return
     */
    @Override
    public int getHeaderLength() {
        // 20 because the minimum TCP header length is 20 - ALWAYS
        return 20 + (this.options != null ? this.options.capacity() : 0);
    }

    @Override
    public boolean isNS() {
        try {
            final byte b = this.headers.getByte(12);
            return (b & 0x01) == 0x01;
        } catch (final Exception e) {
            // ignore, Shouldn't happen since we have already
            // framed all the bytes
            return false;
        }
    }

    @Override
    public boolean isFIN() {
        try {
            final byte b = this.headers.getByte(13);
            return (b & 0x01) == 0x01;
        } catch (final Exception e) {
            // ignore, Shouldn't happen since we have already
            // framed all the bytes
            return false;
        }
    }

    @Override
    public boolean isSYN() {
        try {
            final byte b = this.headers.getByte(13);
            return (b & 0x02) == 0x02;
        } catch (final Exception e) {
            return false;
        }
    }

    @Override
    public boolean isRST() {
        try {
            final byte b = this.headers.getByte(13);
            return (b & 0x04) == 0x04;
        } catch (final Exception e) {
            return false;
        }
    }

    /**
     * Check whether the psh (push) flag is turned on
     * 
     * @return
     */
    @Override
    public boolean isPSH() {
        try {
            final byte b = this.headers.getByte(13);
            return (b & 0x08) == 0x08;
        } catch (final Exception e) {
            return false;
        }
    }

    @Override
    public boolean isACK() {
        try {
            final byte b = this.headers.getByte(13);
            return (b & 0x10) == 0x10;
        } catch (final Exception e) {
            return false;
        }
    }

    @Override
    public boolean isURG() {
        try {
            final byte b = this.headers.getByte(13);
            return (b & 0x20) == 0x20;
        } catch (final Exception e) {
            return false;
        }
    }

    @Override
    public boolean isECE() {
        try {
            final byte b = this.headers.getByte(13);
            return (b & 0x40) == 0x40;
        } catch (final Exception e) {
            return false;
        }
    }

    @Override
    public boolean isCWR() {
        try {
            final byte b = this.headers.getByte(13);
            return (b & 0x80) == 0x80;
        } catch (final Exception e) {
            return false;
        }
    }

    @Override
    public long getSequenceNumber() {
        return this.headers.slice(4, 8).readUnsignedInt();
    }

    @Override
    public long getAcknowledgementNumber() {
        return this.headers.slice(8, 12).readUnsignedInt();
    }

    @Override
    public TransportPacket clone() {
        final IPPacket parent = getParentPacket().clone();
        final Buffer options = this.options != null ? this.options.clone() : null;
        return new TcpPacketImpl(parent, this.headers.clone(), options, getPayload().clone());
    }

    @Override
    public final void write(final OutputStream out, final Buffer payload) throws IOException {
        // TODO: options must be written out as well
        getParentPacket().write(out, Buffers.wrap(this.headers, payload));
    }
}
