package io.pkts.gtp;

import io.pkts.gtp.control.Gtp1Header;
import io.pkts.gtp.control.Gtp2Header;
import io.snice.buffer.Buffer;
import io.snice.buffer.ReadableBuffer;

import static io.snice.preconditions.PreConditions.assertNotNull;

public interface GtpHeader {

    static GtpHeader frame(final Buffer buffer) throws IllegalArgumentException, GtpParseException {
        assertNotNull(buffer, "The buffer cannot be null");
        return frame(buffer.toReadableBuffer());
    }

    static GtpHeader frame(final ReadableBuffer buffer) throws IllegalArgumentException, GtpParseException {
        assertNotNull(buffer, "The buffer cannot be null");
        final byte flags = buffer.getByte(buffer.getReaderIndex());
        final int version = (flags & 0b11100000) >> 5;
        switch (version) {
            case 1:
                return Gtp1Header.frame(buffer);
            case 2:
                return Gtp2Header.frame(buffer);
            default:
                throw new GtpParseException(buffer.getReaderIndex(), "Unknown (" + version + ") GTP protocol version");
        }
    }

    default Gtp1Header toGtp1Header() throws ClassCastException {
        throw new ClassCastException("Unable to cast a " + getClass().getName() + " into a " + Gtp1Header.class.getName());
    }

    default Gtp2Header toGtp2Header() throws ClassCastException {
        throw new ClassCastException("Unable to cast a " + getClass().getName() + " into a " + Gtp2Header.class.getName());
    }

    int getVersion();

    /**
     * This is the length as read in the GTP packet itself. According to specification,
     * it excludes the mandatory fields of the GTP header (but in GTPv2, not really), which makes this length
     * somewhat useless. But to follow standard, this is what this one returns and of course,
     * it is slightly different between GTPv1 and v2.
     *
     * <p>
     * For GTPv1, see section 6 in TS 29.060, which reads:</br>
     * <p>
     * Length: This field indicates the length in octets of the payload, i.e. the rest of the packet following the mandatory
     * part of the GTP header (that is the first 8 octets). The Sequence Number, the N-PDU Number or any Extension
     * headers shall be considered to be part of the payload, i.e. included in the length count.
     * </p>
     *
     * <p>
     * For GTPv2, See section 5.5.1 in TS 23.274, which reads:</br>
     * <p>
     * Octets 3 to 4 represent the Message Length field. This field shall indicate the length of the message in octets
     * excluding the mandatory part of the GTP-C header (the first 4 octets). The TEID (if present) and the Sequence
     * Number shall be included in the length count. The format of the Length field of information elements is specified
     * in subclause 8.2 "Information Element Format".
     * <p>
     * The annoying part about the GTPv2 one is that the Seq No is actually mandatory but because they
     * made the TEID optional BUT placed in between the mandatory parts, I guess they made this weird decision.
     * </p>
     *
     * <p>
     * Anyway, you probably want to use the more useful {@link #getTotalLength()} and {@link #getBodyLength()}.
     * </p>
     */
    int getLength();

    /**
     * The total length is the length in bytes of the entire GTP mesesage, including the GTP header.
     */
    int getTotalLength();

    /**
     * The length of the GTP body only (in bytes). This excludes anything that is part of the GTP header.
     *
     * @return
     */
    int getBodyLength();

    /**
     *
     */
    int getMessageTypeDecimal();

}
