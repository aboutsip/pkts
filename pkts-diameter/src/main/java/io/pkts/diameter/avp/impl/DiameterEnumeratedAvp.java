package io.pkts.diameter.avp.impl;

import io.pkts.diameter.avp.Avp;
import io.pkts.diameter.avp.FramedAvp;
import io.pkts.diameter.avp.type.Enumerated;

public class DiameterEnumeratedAvp<T extends Enum<T>> extends ImmutableAvp<Enumerated<T>> {

    public boolean isEnumerated() {
        return true;
    }

    public Avp<Enumerated<T>> toEnumerated() {
        return this;
    }

    public DiameterEnumeratedAvp(final FramedAvp raw, final Enumerated<T> e) {
        super(raw, e);
    }
}
