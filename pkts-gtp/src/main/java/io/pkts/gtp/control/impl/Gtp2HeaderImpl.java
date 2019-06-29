package io.pkts.gtp.control.impl;

import io.pkts.gtp.GtpVersionException;
import io.pkts.gtp.Teid;
import io.pkts.gtp.control.Gtp2Header;
import io.snice.buffer.Buffer;
import io.snice.buffer.ReadableBuffer;

import java.util.Optional;

import static io.snice.preconditions.PreConditions.assertArgument;
import static io.snice.preconditions.PreConditions.assertNotNull;

public class Gtp2HeaderImpl implements Gtp2Header {

    /**
     * Contains the entire raw GTPv2 header, which is always 8 or 12
     * bytes long, depending on whether the TEID is present or not.
     * <p>
     * And the f@#$4 dumb thing is that instead of having the optional
     * TEID at the end of those bytes, it is in the middle, which means that
     * the sequence no etc, which are always present, are now in different
     * directions. The people that defines standards are certainly not
     * developers!
     */
    private final Buffer header;
    private final Optional<Teid> teid;

    /**
     * Frame the buffer into a {@link Gtp2Header}. A {@link Gtp2Header} is either 8 or 12 bytes long
     * depending if the TEID is present or not.
     *
     * @param buffer
     * @return
     */
    public static Gtp2Header frame(final ReadableBuffer buffer) {
        assertNotNull(buffer, "The buffer cannot be null");
        assertArgument(buffer.capacity() >= 8, "The minimum no of bytes for a GTP header is 8 bytes. This buffer contains less");

        final byte flags = buffer.getByte(0);
        final int version = (flags & 0b11100000) >> 5;
        if (version != 2) {
            throw new GtpVersionException(2, version);
        }

        final boolean teidFlag = (flags & 0b00001000) == 0b00001000;
        final Buffer header = teidFlag ? buffer.readBytes(12) : buffer.readBytes(8);

        final Optional<Teid> teid;
        if (teidFlag) {
            teid = Optional.of(Teid.of(header.slice(4, 8)));
        } else {
            teid = Optional.empty();
        }

        return new Gtp2HeaderImpl(header, teid);
    }

    private Gtp2HeaderImpl(final Buffer header, final Optional<Teid> teid) {
        this.header = header;
        this.teid = teid;
    }

    @Override
    public int getLength() {
        return header.getUnsignedShort(2);
    }

    @Override
    public int getMessageTypeDecimal() {
        return Byte.toUnsignedInt(header.getByte(1));
    }

    @Override
    public Optional<Teid> getTeid() {
        return teid;
    }


}
