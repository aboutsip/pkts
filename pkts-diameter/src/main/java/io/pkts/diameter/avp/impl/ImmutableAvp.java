package io.pkts.diameter.avp.impl;

import io.pkts.buffer.Buffer;
import io.pkts.diameter.avp.Avp;
import io.pkts.diameter.avp.AvpHeader;
import io.pkts.diameter.avp.RawAvp;
import io.pkts.diameter.avp.type.DiameterType;

public class ImmutableAvp<T extends DiameterType> implements Avp<T> {

    private final RawAvp raw;
    private final T value;

    public ImmutableAvp(final RawAvp raw, final T value) {
        this.raw = raw;
        this.value = value;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public int getPadding() {
        return raw.getPadding();
    }

    @Override
    public AvpHeader getHeader() {
        return raw.getHeader();
    }

    @Override
    public Buffer getData() {
        return raw.getData();
    }

    @Override
    public Avp<T> parse() {
        return this;
    }
}
