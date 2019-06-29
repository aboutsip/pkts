package io.pkts.gtp.impl;

import io.pkts.gtp.Teid;
import io.snice.buffer.Buffer;
import io.snice.preconditions.PreConditions;

public class TeidImpl implements Teid {

    public static Teid of(final Buffer buffer) {
        PreConditions.assertNotNull(buffer, "Buffer cannot be null");
        PreConditions.assertArgument(buffer.capacity() == 4, "The length of the TEID must be 4 bytes");
        return new TeidImpl(buffer);
    }

    private final Buffer buffer;

    private TeidImpl(final Buffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final TeidImpl teid = (TeidImpl) o;
        return buffer.equals(teid.buffer);
    }

    @Override
    public int hashCode() {
        return buffer.hashCode();
    }
}

