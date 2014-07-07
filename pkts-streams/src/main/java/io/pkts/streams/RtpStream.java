package io.pkts.streams;

import io.pkts.packet.rtp.RtpPacket;

/**
 * An {@link RtpStream} represents a stream of {@link RtpPacket}s having the same SSRC
 * (Synchronization source - see RFC3550). Typically, all packets (which is pretty much always using
 * UDP as the transport) are sent/received from the same local/remote port pair. However, it is
 * possible that those ip-ports pairs are changed during the life span of the {@link RtpStream}.
 * E.g., two users may first establish an {@link RtpStream} between them using a protocol such as
 * SIP but perhaps once of the parties moves from one wifi network to another and therefore the
 * ip-address and port of that user has to change in order for the stream to continue.
 * 
 * @author jonas@jonasborjesson.com
 */
public interface RtpStream extends Stream<RtpPacket> {

    long getMaxJitter();

    long getMeanJitter();

    long getMaxDelta();

    long getLostPackets();

    long getSequenceErrors();

}
