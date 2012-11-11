/**
 * 
 */
package com.aboutsip.yajpcap.packet.rtp;

import java.io.IOException;

import com.aboutsip.yajpcap.packet.impl.ApplicationPacket;

/**
 * @author jonas@jonasborjesson.com
 */
public interface RtpPacket extends ApplicationPacket {

    int getVersion() throws IOException;

    boolean hasPadding() throws IOException;

    boolean hasExtensions() throws IOException;

    boolean hasMarker() throws IOException;

    int getPayloadType() throws IOException;

    int getSeqNumber() throws IOException;

    long getTimestamp() throws IOException;

    long getSyncronizationSource() throws IOException;

    int getContributingSource() throws IOException;

}
