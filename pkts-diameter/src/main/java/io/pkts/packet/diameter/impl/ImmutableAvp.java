package io.pkts.packet.diameter.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.diameter.Avp;
import io.pkts.packet.diameter.AvpHeader;

public class ImmutableAvp implements Avp {

    private final AvpHeader header;
    private final Buffer data;

    public ImmutableAvp(final AvpHeader header, final Buffer data) {
        this.header = header;
        this.data = data;
    }

    /**
     * From RFC 6733:
     * <p>
     * Each AVP of type OctetString MUST be padded to align on a 32-bit
     * boundary, while other AVP types align naturally.  A number of zero-
     * valued bytes are added to the end of the AVP Data field until a word
     * boundary is reached.  The length of the padding is not reflected in
     * the AVP Length field.
     *
     * @return
     */
    @Override
    public int getPadding() {
        final int padding = header.getLength() % 4;
        if (padding != 0) {
            return 4 - padding;
        }
        return 0;
    }

    public String toString() {
        return header.toString();
    }

    @Override
    public AvpHeader getHeader() {
        return header;
    }

    @Override
    public Buffer getData() {
        return data;
    }
}
