package io.pkts.gtp;

import io.pkts.gtp.impl.TeidImpl;
import io.snice.buffer.Buffer;
import io.snice.preconditions.PreConditions;

/**
 * Tunnel Endpoint identifier is used to multiplex different connections across
 * the same GTP tunnel.
 */
public interface Teid {

    static Teid of(final Buffer buffer) {
        return TeidImpl.of(buffer);
    }

}
