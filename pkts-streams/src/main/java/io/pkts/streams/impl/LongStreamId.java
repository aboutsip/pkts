/**
 * 
 */
package io.pkts.streams.impl;

import io.pkts.streams.StreamId;

/**
 * @author jonas
 *
 */
public final class LongStreamId implements StreamId {

    private final long id;

    /**
     * 
     */
    public LongStreamId(final long id) {
        this.id = id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String asString() {
        return Long.toString(this.id);
    }

    public long getId() {
        return this.id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return 31 + (int) (this.id ^ this.id >>> 32);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LongStreamId other = (LongStreamId) obj;
        return this.id != other.id;
    }

}
