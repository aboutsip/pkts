/**
 * 
 */
package com.aboutsip.yajpcap.framer;

import java.io.IOException;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.frame.Layer2Frame;
import com.aboutsip.yajpcap.frame.Layer3Frame;

/**
 * A layer 3 framer will frame a buffer into a layer 3 frame
 * 
 * @author jonas@jonasborjesson.com
 */
public interface Layer3Framer extends Framer<Layer2Frame> {

    @Override
    Layer3Frame frame(Layer2Frame parent, Buffer buffer) throws IOException;

}
