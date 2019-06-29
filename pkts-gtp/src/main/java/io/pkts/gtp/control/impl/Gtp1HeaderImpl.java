package io.pkts.gtp.control.impl;

import io.pkts.gtp.GtpVersionException;
import io.pkts.gtp.Teid;
import io.pkts.gtp.control.Gtp1Header;
import io.snice.buffer.Buffer;
import io.snice.buffer.ReadableBuffer;

import static io.snice.preconditions.PreConditions.assertArgument;
import static io.snice.preconditions.PreConditions.assertNotNull;

public class Gtp1HeaderImpl implements Gtp1Header {

    private final Buffer header;
    private final Teid teid;

    private Gtp1HeaderImpl(final Buffer header, final Teid teid) {
        this.header = header;
        this.teid = teid;
    }

    /**
     * Frame the buffer into a {@link Gtp1Header}. A {@link Gtp1Header} is either 8 or 12 bytes long
     * depending if any of the optional sequence no, extension header or n-pdu flags are present. Note
     * that even if a single one of those flags are present, the header will be an extra 4 bytes long because,
     * according to TS 29.274 section 5.1:
     * <p>
     * "Control Plane GTP header length shall be a multiple of 4 octets"
     *
     * @param buffer
     * @return
     */
    public static Gtp1Header frame(final ReadableBuffer buffer) {
        assertNotNull(buffer, "The buffer cannot be null");
        assertArgument(buffer.capacity() >= 8, "The minimum no of bytes for a GTP header is 8 bytes. This buffer contains less");

        final byte flags = buffer.getByte(0);
        final int version = (flags & 0b11100000) >> 5;
        if (version != 1) {
            throw new GtpVersionException(1, version);
        }

        // if any of the sequence no, extension or n-pdu flags are "on" then we have an additional
        // 4 bytes in the header, hence, a "long" header
        final boolean longHeader = ((flags & 0b00000100) == 0b00000100)
                || ((flags & 0b00000010) == 0b00000010)
                || ((flags & 0b00000001) == 0b00000001);

        final Buffer header = longHeader ? buffer.readBytes(12) : buffer.readBytes(8);
        final Teid teid = Teid.of(header.slice(4, 8));
        return new Gtp1HeaderImpl(header, teid);
    }

    @Override
    public Teid getTeid() {
        return teid;
    }

    @Override
    public int getLength() {
        return header.getUnsignedShort(2);
    }

    @Override
    public int getMessageTypeDecimal() {
        return Byte.toUnsignedInt(header.getByte(1));
    }

}
