package io.pkts.diameter.avp.impl;

import io.pkts.diameter.avp.FramedAvp;
import io.pkts.diameter.avp.type.Grouped;

public class DiameterGroupedAvp extends ImmutableAvp<Grouped> {

    public DiameterGroupedAvp(final FramedAvp raw) {
        super(raw, Grouped.parse(raw));
    }
}
