/**
 * 
 */
package com.aboutsip.yajpcap.framer;

import java.io.IOException;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.frame.Frame;
import com.aboutsip.yajpcap.protocol.Protocol;

/**
 * Simple interface for framers.
 * 
 * @author jonas@jonasborjesson.com
 */
public interface Framer {

    /**
     * 
     * @return the protocol this framer is capable of framing
     */
    Protocol getProtocol();

    /**
     * Ask the framer to frame the buffer into a frame.
     * 
     * @param buffer the buffer containing all the raw data
     * @return a new frame
     * @throws IOException in case something goes wrong when reading data from
     *             the buffer
     */
    Frame frame(Buffer buffer) throws IOException;

    /**
     * Check whether the supplied data could be framed into a frame of this
     * type.
     * 
     * @param data the data to check whether it could be a frame of this type
     * @return true if the data indeed could be of this framer type, false if
     *         not
     */
    boolean accept(Buffer data) throws IOException;

}
