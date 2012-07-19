/**
 * 
 */
package com.aboutsip.yajpcap.packet;

import java.text.SimpleDateFormat;

/**
 * @author jonas@jonasborjesson.com
 */
public interface PCapPacket extends Packet {

    /**
     * The arrival time of this packet in microseconds relative to epoch
     * (midnight UTC of January 1, 1970).
     * 
     * Note, since this returns with microseconds precision (which may or may
     * not be relevant depending on the hardware on which the packet was
     * captured on) and you wish to format this arrival time into a more human
     * readable format you could use the {@link SimpleDateFormat} but it can
     * only handle milliseconds precision (you will have to write your own date
     * formatter if you want microseconds).
     * 
     * Here is a snippet illustrating how to turn the arrival time of the packet
     * into a human readable date
     * 
     * <pre>
     * Packet p = ...;
     * SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");
     * Date date = new Date(p.getArrivalTime() / 1000);
     * System.out.println("Arrival time: " + formatter.format(date));
     * </pre>
     * 
     * Note how a integer devision is performed on the arrival time to "cut off"
     * the microseconds from the time stamp
     * 
     * @return the arrival time of the packet in milliseconds since the start of
     *         the epoch
     */
    long getArrivalTime();

    /**
     * Get the total length of the data. Not all of that data may have been
     * captured in this one frame, which is evident if the actual captured
     * length is different from the total length
     * 
     * @return
     */
    long getTotalLength();

    /**
     * Get the actual length of what is contained in this frame. Note, if the
     * captured length is different from the total length then we have a
     * fragmented packet
     * 
     * @return the length in bytes
     */
    long getCapturedLength();

}
