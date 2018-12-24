package io.pkts.diameter.avp.impl;

import io.pkts.diameter.avp.FramedAvp;
import io.pkts.diameter.avp.type.DiameterIdentity;

public class DiameterIdentityAvp extends ImmutableAvp<DiameterIdentity> {

    public DiameterIdentityAvp(final FramedAvp raw) {
        super(raw, DiameterIdentity.parse(raw.getData()));
    }
}
