/**
 * 
 */
package io.pkts.streams.impl;

import io.pkts.streams.StreamId;

/**
 * A simple {@link StreamId} that simply wraps a {@link String}.
 * 
 * @author jonas@jonasborjesson.com
 */
public final class StringStreamId implements StreamId {

    private final String id;

    /**
     * 
     */
    public StringStreamId(final String id) {
        this.id = id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String asString() {
        return this.id;
    }

    @Override
    public String toString() {
        return this.id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.id == null ? 0 : this.id.hashCode());
        return result;
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
        final StringStreamId other = (StringStreamId) obj;
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        return true;
    }

}
