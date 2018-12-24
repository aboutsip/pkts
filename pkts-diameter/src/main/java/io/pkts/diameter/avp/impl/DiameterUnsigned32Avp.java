package io.pkts.diameter.avp.impl;

import io.pkts.diameter.avp.FramedAvp;
import io.pkts.diameter.avp.type.Unsigned32;

public class DiameterUnsigned32Avp extends ImmutableAvp<Unsigned32> {

    public DiameterUnsigned32Avp(final FramedAvp raw) {
        super(raw, Unsigned32.parse(raw.getData()));
    }
}
