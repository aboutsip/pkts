/**
 * 
 */
package io.pkts.streams;

import io.pkts.packet.IPPacket;

/**
 * When an {@link IPPacket} is detected to to be fragemented, the corresponding
 * {@link IPFrame} will be passed to this interface and it is up to this
 * listener to defragment the IP-packet. The reason why this is not included in
 * the core yajpcap library is because sometimes fragments get lost or you
 * simply only captured all udp packets on a specific port and when the IP
 * packet is fragmented, the second fragment will not contain any UDP
 * information anymore and therefore these packets will not be included in your
 * pcap. However, for some applications it may be ok to only examine a portion
 * of the packet in order to make a more intelligent decision than the stack
 * itself could. Consider the following example:
 * 
 * You are building a little program that figures out how long a phone call in
 * your system is on average. It accomplishes this by taking in a huge pcap cap
 * and will build up call information based on that. The pcap command you are
 * running on your system may look something like this:
 * 
 * <pre>
 * tcpdump -n -v -s0 -w mytraffic.pcap port 5060
 * </pre>
 * 
 * which would capture all SIP traffic on its standard port no matter if it is
 * UDP or TCP. However, due to fragmentation the second fragmented packet will
 * only contain IP information, which doesn't have any port information in it
 * and therefore this crucial packet will NOT be included in your pcap. As such,
 * if the yajpcap core would try and reassemble this information, it would not
 * succeed and would most likely then have to discard the information, meaning
 * that your program would never receive this information.
 * 
 * However, in this case of this "what's my average call length" program, it is
 * actually enough if we can determine if this fragmented packet is e.g. an
 * INVITE and its Call-ID and then we actually have enough information to
 * determine the "start event" of that call. Hence, it may very well be ok if
 * those missing fragments never are processed.
 * 
 * Because of this, it seems better to leave this functionality out of the core
 * yajpcap library and rather provide the developer with this interface instead
 * and also to provide with some sensible default values for you to use if you
 * are ok with the default behavior.
 * 
 * @author jonas@jonasborjesson.com
 */
public interface FragmentListener {

    /**
     * Whenever a fragmented {@link IPFrame} is detected, the registered
     * {@link FragmentListener} will be consulted. It is up to this listener to
     * decide to either hold on to the fragment until it can re-structure the
     * entire frame, or if perhaps this was the last frame needed and as such
     * restructures the frame and returns the re-constructed frame.
     * 
     * Note, if the implementing class let's any exceptions escape from this
     * method then will just be caught and logged and the {@link IPFrame} will
     * be dropped so please make sure that you handle all exceptions.
     * 
     * 
     * @param ipFrame
     *            the fragment.
     * @return null if there is not enough information accumulated by the
     *         listener in order to re-construct the full frame or an
     *         {@link IPFrame}. If an {@link IPFrame} is returned, this frame
     *         will then be processed like any other frame which then most
     *         likely will end up in registered {@link StreamListener}s. Hence,
     *         even though it is possible to simply just return whatever the
     *         listener received, the application must then be prepared to
     *         receive fragmented frames.
     */
    IPPacket handleFragment(IPPacket ipPacket);

}
