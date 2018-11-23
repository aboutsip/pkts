package io.pkts.packet.diameter.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.diameter.Avp;
import io.pkts.packet.diameter.DiameterHeader;
import io.pkts.packet.diameter.DiameterMessage;

import java.util.List;

public class ImmutableDiameterMessage implements DiameterMessage {

    /**
     * The full raw diameter message.
     */
    private final Buffer raw;

    private final DiameterHeader header;
    private final List<Avp> avps;

    public ImmutableDiameterMessage(final Buffer raw, final DiameterHeader header, final List<Avp> avps) {
        this.raw = raw;
        this.header = header;
        this.avps = avps;
    }

    @Override
    public DiameterHeader getHeader() {
        return header;
    }

    @Override
    public List<Avp> getAllAvps() {
        return avps;
    }
}
