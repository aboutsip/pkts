/**
 * 
 */
package io.pkts.packet;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

/**
 * Represents a captured packet.
 * 
 * @author jonas@jonasborjesson.com
 */
public interface Packet extends Cloneable {
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
     * Note how an integer devision is performed on the arrival time to
     * "cut off" the microseconds from the time stamp
     * 
     * @return the arrival time of the packet in microseconds since the start of
     *         the epoch
     */
    long getArrivalTime();

    /**
     * Calling this method will force the packet to completely parse its data
     * and check so that all the information conforms to whatever rules this
     * packet needs to follow. E.g., if this happens to be a SIP packet, then it
     * will check if it has the mandatory headers etc.
     * 
     * Some simpler packets, such as the {@link IPPacket}, hardly does anything
     * in this method but more complex protocols such as SIP (once again), HTTP
     * etc can spend quite some time verifying everything, which is why you
     * don't want to do it unless you really have to.
     * 
     * In general, yajpcap has the philosophy of
     * "assume that everything is ok until things blow up and then deal with it"
     */
    void verify();

    void write(OutputStream out) throws IOException;

    Packet clone();

}
