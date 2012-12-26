/**
 * 
 */
package com.aboutsip.streams;

import com.aboutsip.yajpcap.FrameHandler;
import com.aboutsip.yajpcap.packet.Packet;

/**
 * The {@link StreamHandler} is a higher-level {@link FrameHandler} that
 * consumes streams and calls its registered {@link StreamListener}s.
 * 
 * @author jonas@jonasborjesson.com
 */
public interface StreamHandler extends FrameHandler {

    /**
     * Add a {@link StreamListener} to this {@link StreamHandler}.
     * 
     * @param listener
     * @throws IllegalArgumentException
     *             in case the {@link StreamListener} is not propertly
     *             parameterized.
     */
    void addStreamListener(StreamListener<? extends Packet> listener) throws IllegalArgumentException;

}
