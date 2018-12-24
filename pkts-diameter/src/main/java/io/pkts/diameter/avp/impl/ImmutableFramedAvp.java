package io.pkts.diameter.avp.impl;

import io.pkts.buffer.Buffer;
import io.pkts.diameter.avp.Avp;
import io.pkts.diameter.avp.AvpHeader;
import io.pkts.diameter.avp.FramedAvp;
import io.pkts.diameter.impl.DiameterParser;

public class ImmutableFramedAvp implements FramedAvp {

    private final AvpHeader header;
    private final Buffer data;

    public ImmutableFramedAvp(final AvpHeader header, final Buffer data) {
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

    @Override
    public String toString() {
        return header.toString();
    }

    @Override
    public AvpHeader getHeader() {
        return header;
    }

    @Override
    public Buffer getData() {
        // must slice so that the returned data has it's own reader index etc.
        return data.slice();
    }

    @Override
    public Avp parse() {
        return DiameterParser.parseAvp(this);
    }
}
