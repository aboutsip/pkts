/**
 * 
 */
package com.aboutsip.yajpcap.frame;

import java.io.IOException;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.protocol.Protocol;

/**
 * A frame in the ISO stack typically have some headers and then a payload/data
 * section. The payload may itself be another frame and so on.
 * 
 * @author jonas@jonasborjesson.com
 * 
 */
public interface Frame {

    /**
     * Check whether this frame contains a particular protocol. This will cause
     * the frame to examine all the containing frames to check whether they are
     * indeed the protocol the user asked for.
     * 
     * @param p
     * @return
     * @throws IOException in case something goes wrong when framing the rest of
     *             the protocol stack
     */
    boolean hasProtocol(Protocol p) throws IOException;

    /**
     * Get the protocol of this frame.
     * 
     * @return
     */
    Protocol getProtocol();

    /**
     * Find the frame for protocol p.
     * 
     * @param p
     * @return the frame that encapsulates the protocol or null if this protocol
     *         doesn't exist
     * @throws IOException in case something goes wrong when framing the rest of
     *             the protocol stack
     */
    Frame getFrame(Protocol p) throws IOException;

    /**
     * Frame all protocols within this frame. Typically, once a particular frame
     * has been framed, it will not continue down its payload to figure out what
     * other frames may potentially be contained in the payload. Eveyrhing is
     * done lazily.
     */
    void frameAll() throws IOException;

    /**
     * Get the name of the frame. Wireshark will give you a short description of
     * all the known protocols within its "super" frame. E.g., if you "click" on
     * the Pcap Frame it will have a field called "protocols in frame" and will
     * display something like "eth:ip:udp:sip:sdp", this function will return a
     * short name like that.
     * 
     * @return
     */
    String getName();

    /**
     * Get the next frame, or null if there is none. Note, if there isn't
     * another frame but there is still raw data within this frame, it only
     * means that we didn't recognize the payload.
     * 
     * @return
     * @throws IOException
     */
    Frame getNextFrame() throws IOException;

    /**
     * Get the payload of the frame. If null, then this frame doesn't have any
     * payload
     * 
     * @return
     */
    Buffer getData();

}
