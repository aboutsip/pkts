/**
 * 
 */
package io.pkts.packet.rtp;

import io.pkts.packet.impl.ApplicationPacket;

import java.io.IOException;

/**
 * @author jonas@jonasborjesson.com
 */
public interface RtpPacket extends ApplicationPacket {

    @Override
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

}
