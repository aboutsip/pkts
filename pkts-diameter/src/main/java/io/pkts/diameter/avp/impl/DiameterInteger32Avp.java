package io.pkts.diameter.avp.impl;

import io.pkts.diameter.avp.FramedAvp;
import io.pkts.diameter.avp.type.Integer32;

public class DiameterInteger32Avp extends ImmutableAvp<Integer32> {

    public DiameterInteger32Avp(final FramedAvp raw) {
        super(raw, Integer32.parse(raw.getData()));
    }
}
