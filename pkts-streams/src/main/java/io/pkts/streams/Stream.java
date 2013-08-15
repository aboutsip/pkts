/**
 * 
 */
package io.pkts.streams;

import io.pkts.Pcap;
import io.pkts.PcapOutputStream;
import io.pkts.packet.Packet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * A {@link Stream} represents a set of {@link Packet}s that belong together.
 * E.g., in SIP, messages that goes within the same dialog will be grouped into
 * a single stream.
 * 
 * @author jonas@jonasborjesson.com
 */
public interface Stream<T extends Packet> {

    /**
     * Get all {@link Packet}s that belongs to this stream.
     * 
     * @return
     */
    List<T> getPackets();

    /**
     * Get the duration in microseconds of the stream. Note, see comment on
     * {@link Packet#getArrivalTime()} regarding the microsecond precision.
     * 
     * Depending on the underlying protocol this can be as simple as the time
     * between the first to the last packet, which is what the {@link RtpStream}
     * does. Or, it can be more complicated as with a {@link SipStream} that
     * checks the duration of the dialog (if one was established).
     * 
     * @return the duration (in milliseconds) of this {@link Stream}. If the
     *         duration cannot be calulated for whatever reason (no packets at
     *         all? Only one packet?), then -1 (negative one) will be returned.
     */
    long getDuration();

    /**
     * Get the arrival time of the very first packet in this stream.
     * 
     * Note, see comment on {@link Packet#getArrivalTime()} regarding the
     * microsecond precision.
     * 
     * @return the arrival time of the first packet (in microseconds), -1
     *         (negative one) in case this {@link Stream} doesn't contain any
     *         packets.
     */
    long getTimeOfFirstPacket();

    /**
     * Get the arrival time of the last packet in this stream.
     * 
     * Note, see comment on {@link Packet#getArrivalTime()} regarding the
     * microsecond precision.
     * 
     * @return the arrival time of the last packet (in microseconds), -1
     *         (negative one) in case this {@link Stream} doesn't contain any
     *         packets.
     */
    long getTimeOfLastPacket();

    /**
     * Get the unique {@link StreamId} for this particular {@link Stream}.
     * 
     * @return
     */
    StreamId getStreamIdentifier();

    /**
     * Write this {@link Stream} to the specified {@link OutputStream}. Also see
     * {@link Pcap#createOutputStream(OutputStream)}.
     * 
     * @param out
     *            the {@link OutputStream}, which typically is a
     *            {@link PcapOutputStream}.
     * @throws IOException
     */
    void write(OutputStream out) throws IOException;

}
