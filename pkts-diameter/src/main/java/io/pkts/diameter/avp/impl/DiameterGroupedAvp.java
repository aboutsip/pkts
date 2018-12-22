package io.pkts.diameter.avp.impl;

import io.pkts.diameter.avp.RawAvp;
import io.pkts.diameter.avp.type.Grouped;

public class DiameterGroupedAvp extends ImmutableAvp<Grouped> {

    public DiameterGroupedAvp(final RawAvp raw) {
        super(raw, Grouped.parse(raw));
    }
}
