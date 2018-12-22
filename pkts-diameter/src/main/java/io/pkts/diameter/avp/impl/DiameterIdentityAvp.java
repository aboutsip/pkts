package io.pkts.diameter.avp.impl;

import io.pkts.diameter.avp.RawAvp;
import io.pkts.diameter.avp.type.DiameterIdentity;

public class DiameterIdentityAvp extends ImmutableAvp<DiameterIdentity> {

    public DiameterIdentityAvp(final RawAvp raw) {
        super(raw, DiameterIdentity.parse(raw.getData()));
    }
}
