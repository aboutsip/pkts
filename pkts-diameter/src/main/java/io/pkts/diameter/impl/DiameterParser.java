package io.pkts.diameter.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.ReadOnlyBuffer;
import io.pkts.diameter.Avp;
import io.pkts.diameter.AvpHeader;
import io.pkts.diameter.DiameterHeader;
import io.pkts.diameter.DiameterMessage;
import io.pkts.diameter.DiameterParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author jonas@jonasborjesson.com
 */
public class DiameterParser {

    public static DiameterMessage frame(final ReadOnlyBuffer buffer) throws DiameterParseException, IOException {
        final int start = buffer.getReaderIndex();
        final DiameterHeader header = frameHeader(buffer);
        // the +20 because we have already consumed 20 bytes for the header but the length as stated
        // in the diameter header actually includes these 20 bytes so...
        if (header.getLength() > buffer.getReadableBytes() + 20) {
            throw new DiameterParseException(20, "Not enough bytes in message to parse out the full message");
        }

        // TODO: we may want to slice out the entire buffer and store that as well
        // for those applications that just write back out to another network connection
        // and doesn't really need to mess with the data. Kind of what I do for SIP.
        // Also, the way this will be read off of the network, we will probably always have
        // the exact buffer we need and perhaps we should just assume that's the case.

        final ReadOnlyBuffer avps = (ReadOnlyBuffer) buffer.readBytes(header.getLength() - 20);
        final List<Avp> list = new ArrayList<>(); // TODO: what's a sensible default?
        while (avps.getReadableBytes() > 0) {
            final int readerIndex = avps.getReaderIndex();
            final Avp avp = Avp.frame(avps);
            list.add(avp);
            final int padding = avp.getPadding();
            if (padding != 0) {
                avps.readBytes(padding);
            }

            // fail safe - if we are not making any progress
            // then we need to bail out.
            if (readerIndex == avps.getReaderIndex()) {
                throw new DiameterParseException(readerIndex, "Seems like we are stuck parsing " +
                        "AVPs for diameter message " + header.getCommandCode() + ". Bailing out");

            }
        }

        final ReadOnlyBuffer entireMsg = (ReadOnlyBuffer) buffer.slice(start, buffer.getReaderIndex());

        return new ImmutableDiameterMessage(entireMsg, header, list);
    }


    public static DiameterHeader frameHeader(final ReadOnlyBuffer buffer) throws DiameterParseException, IOException {
        if (buffer.getReadableBytes() < 20) {
            throw new DiameterParseException(0, "Cannot be a Diameter message because the header is less than 20 bytes");
        }

        final ReadOnlyBuffer header = (ReadOnlyBuffer) buffer.readBytes(20);
        return new ImmutableDiameterHeader(header);
    }

    /**
     * Convenience method for checking if this could indeed by a {@link DiameterMessage}. Use this when
     * you just want to check and not handle the {@link DiameterParseException} that would be thrown as a
     * result of this not being a diameter message.
     * <p>
     * TODO: may actually need a more specific parse exception because right now, you don't konw if
     * it "blew" up because it is not a diameter message or because there is a "real" parse exception.
     *
     * @param buffer
     * @return
     * @throws IOException
     */
    public static boolean couldBeDiameterMessage(final ReadOnlyBuffer buffer) throws IOException {
        final int index = buffer.getReaderIndex();
        try {
            final DiameterHeader header = frameHeader(buffer);
            return header.validate();
        } catch (final DiameterParseException e) {
            return false;
        } finally {
            buffer.setReaderIndex(index);
        }
    }

    public static Avp frameAvp(final ReadOnlyBuffer buffer) throws DiameterParseException {
        try {
            final AvpHeader header = frameAvpHeader(buffer);
            final int avpHeaderLength = header.isVendorSpecific() ? 12 : 8;
            final Buffer data = buffer.readBytes(header.getLength() - avpHeaderLength);
            return new ImmutableAvp(header, data);
        } catch (final IndexOutOfBoundsException | IOException e) {
            // not enough data
            throw new DiameterParseException("Not enough data in buffer to read out the full AVP");
        }
    }

    public static AvpHeader frameAvpHeader(final ReadOnlyBuffer buffer) throws DiameterParseException {
        try {
            if (buffer.getReadableBytes() < 8) {
                throw new DiameterParseException("Unable to read 8 bytes from the buffer, not enough data to parse AVP.");
            }

            // these are the flags and we need to check if the Vendor-ID bit is set and if so we need
            // another 4 bytes for the AVP Header.
            final byte flags = buffer.getByte(buffer.getReaderIndex() + 4);
            final boolean isVendorIdPresent = (flags & 0b10000000) == 0b10000000;
            final Buffer avpHeader = isVendorIdPresent ? buffer.readBytes(12) : buffer.readBytes(8);
            final Optional<Long> vendorId = isVendorIdPresent ? Optional.of(avpHeader.getUnsignedInt(8)) : Optional.empty();
            return new ImmutableAvpHeader(avpHeader, vendorId);

        } catch (final IOException e) {
            throw new DiameterParseException("Not enough data in the buffer in order to parse the AVP Header.");
        }
    }

    /**
     * It is quite common in the various diameter headers that the length is stored in 3 octects. This will return
     * an int based on those.
     *
     * @param a
     * @param b
     * @param c
     * @return
     */
    public static int getIntFromThreeOctets(final byte a, final byte b, final byte c) {
        return (a & 0xff) << 16 | (b & 0xff) << 8 | (c & 0xff) << 0;
    }
}
