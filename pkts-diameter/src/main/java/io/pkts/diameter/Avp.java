package io.pkts.diameter;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.ReadOnlyBuffer;
import io.pkts.diameter.impl.DiameterParser;

import java.io.IOException;

public interface Avp {

    static Avp frame(final ReadOnlyBuffer buffer) throws DiameterParseException, IOException {
        return DiameterParser.frameAvp(buffer);
    }

    /**
     * Return the amount of padding needed for this AVP.
     *
     * @return
     */
    int getPadding();

    AvpHeader getHeader();

    Buffer getData();
}
