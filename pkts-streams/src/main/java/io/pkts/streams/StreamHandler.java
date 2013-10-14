/**
 * 
 */
package io.pkts.streams;

import io.pkts.PacketHandler;
import io.pkts.packet.Packet;
import io.pkts.packet.sip.SipPacket;

import java.util.Map;


/**
 * The {@link StreamHandler} is a higher-level {@link PacketHandler} that
 * consumes streams and calls its registered {@link StreamListener}s.
 * 
 * @author jonas@jonasborjesson.com
 */
public interface StreamHandler extends PacketHandler {

    /**
     * Add a {@link StreamListener} to this {@link StreamHandler}.
     * 
     * @param listener
     * @throws IllegalArgumentException
     *             in case the {@link StreamListener} is not propertly
     *             parameterized.
     */
    void addStreamListener(StreamListener<? extends Packet> listener) throws IllegalArgumentException;

    /**
     * Set the {@link FragmentListener}. Note, only one of these listeners is
     * allowed so if you set a second one the previous listener will be thrown
     * away.
     * 
     * @param listener
     *            the listener or null if you want to remove a previously set
     *            listener.
     */
    void setFragmentListener(FragmentListener listener);

    /**
     * If there is a registered {@link StreamListener} for {@link SipPacket}s
     * then this {@link StreamHandler} will start processing SIP messages for
     * which you can get all the statistics for through this method.
     * 
     * @return a {@link SipStatistics} object. Note, if this
     *         {@link StreamHandler} has not been configured to handle sip
     *         traffic then all the stats will be zero.
     */
    SipStatistics getSipStatistics();

    /**
     * Through the {@link StreamListener#startStream(Stream, Packet)} and
     * {@link StreamListener#endStream(Stream)} you will get notified when a
     * stream starts and when it ends. However, at the end of a run there may be
     * many streams that are still "running". E.g., in the case of SIP, there
     * are probably many un-terminated INVITE dialogs, i.e. calls that are still
     * in the process of being established or still in call. These streams have
     * not ended yet so at the end of the run you can use this method to get all
     * the streams that are left.
     * 
     * @return
     */
    Map<StreamId, ? extends Stream> getStreams();


}
