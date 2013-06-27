/**
 * 
 */
package io.pkts.streams.impl;

import io.pkts.buffer.Buffer;
import io.pkts.streams.StreamId;


/**
 * @author jonas@jonasborjesson.com
 */
public class BufferStreamId implements StreamId {

    private final Buffer buffer;

    /**
     * 
     */
    public BufferStreamId(final Buffer buffer) {
        this.buffer = buffer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String asString() {
        return this.buffer.toString();
    }

    @Override
    public String toString() {
        return this.buffer.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((this.buffer == null) ? 0 : this.buffer.hashCode());
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
        final BufferStreamId other = (BufferStreamId) obj;
        if (this.buffer == null) {
            if (other.buffer != null) {
                return false;
            }
        } else if (!this.buffer.equals(other.buffer)) {
            return false;
        }
        return true;
    }

}
