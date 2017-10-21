/**
 * 
 */
package io.pkts.packet.rtp;

import java.io.IOException;

import io.pkts.packet.TransportPacket;
import io.pkts.packet.impl.ApplicationPacket;

/**
 * @author jonas@jonasborjesson.com
 */
public interface RtpPacket extends ApplicationPacket {

    int getVersion();

    boolean hasPadding() throws IOException;

    boolean hasExtensions() throws IOException;

    boolean hasMarker() throws IOException;

    int getPayloadType() throws IOException;

    int getSeqNumber() throws IOException;

    long getTimestamp() throws IOException;

    long getSyncronizationSource() throws IOException;

    int getContributingSource() throws IOException;

    /**
     * Dump the entire {@link RtpPacket} as a raw byte-array.
     * 
     * @return
     */
    byte[] dumpPacket();

    @Override
    TransportPacket getParentPacket();
}
