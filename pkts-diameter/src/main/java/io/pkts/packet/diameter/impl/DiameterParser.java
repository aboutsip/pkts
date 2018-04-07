package io.pkts.packet.diameter.impl;

import java.io.IOException;

import io.pkts.buffer.Buffer;
import io.pkts.packet.diameter.DiameterHeader;
import io.pkts.packet.diameter.DiameterMessage;
import io.pkts.packet.diameter.DiameterParseException;

/**
 * @author jonas@jonasborjesson.com
 */
public class DiameterParser {

    public static DiameterMessage frame(final Buffer buffer) throws DiameterParseException, IOException {

        return null;
    }

    public static DiameterHeader frameHeader(final Buffer buffer) throws DiameterParseException, IOException {
        if (!couldBeDiameterHeader(buffer)) {
            throw new DiameterParseException(0, "Cannot be a Diameter message because it is less than 20 bytes");
        }

        final Buffer header = buffer.slice(20);
        return new ImmutableDiameterHeader(header);
    }

    /**
     * Helper function to see if the supplied byte-buffer could be a diameter message. Even if this method
     * returns true, there is no guarantee that it indeed is a Diameter message but if it doesn't go through,
     * then it is definitely NOT a Diameter message.
     *
     * @param buffer
     * @return true if this could potentially be a diameter message, false if it def is not a diameter message.
     * @throws IOException
     */
    public static boolean couldBeDiameterMessage(final Buffer buffer) throws IOException {

        if (!couldBeDiameterHeader(buffer)) {
            return false;
        }

        // perhaps more stuff? Checking version?
        return true;
    }

    /**
     * A diameter message must be at least 20 bytes long. This is then just
     * diameter header and no AVPs. I guess one could argue there must also
     * be at least one AVP but we'll add that later if that is necessary.
     */
    public static boolean couldBeDiameterHeader(final Buffer buffer) throws IOException {
        return buffer.getReadableBytes() >= 20;
    }
}
