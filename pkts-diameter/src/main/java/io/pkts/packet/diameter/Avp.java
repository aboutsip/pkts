package io.pkts.packet.diameter;

import io.pkts.buffer.Buffer;
import io.pkts.packet.diameter.impl.DiameterParser;

import java.io.IOException;

public interface Avp {

    static Avp frame(final Buffer buffer) throws DiameterParseException, IOException {
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
