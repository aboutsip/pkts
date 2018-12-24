package io.pkts.diameter.impl;

import io.pkts.buffer.ReadOnlyBuffer;
import io.pkts.diameter.DiameterHeader;
import io.pkts.diameter.DiameterMessage;
import io.pkts.diameter.avp.FramedAvp;
import io.pkts.diameter.avp.OriginHost;
import io.pkts.diameter.avp.OriginRealm;

import java.util.List;

public class ImmutableDiameterMessage implements DiameterMessage {

    /**
     * The full raw diameter message.
     */
    private final ReadOnlyBuffer raw;

    private final DiameterHeader header;
    private final List<FramedAvp> avps;

    public ImmutableDiameterMessage(final ReadOnlyBuffer raw, final DiameterHeader header, final List<FramedAvp> avps) {
        this.raw = raw;
        this.header = header;
        this.avps = avps;
    }

    @Override
    public DiameterHeader getHeader() {
        return header;
    }

    @Override
    public List<FramedAvp> getAllAvps() {
        return avps;
    }

    /**
     * This class is immutable and as such, when cloning, you'll just get back the same
     * regference again.
     *
     * @return
     */
    @Override
    public DiameterMessage clone() {
        return this;
    }

    @Override
    public OriginHost getOriginHost() {
        return null;
    }

    @Override
    public OriginRealm getOriginRealm() {
        return null;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(header.toString());
        sb.append(", AVP Count: ").append(avps.size());
        return sb.toString();
    }
}
