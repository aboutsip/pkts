/**
 * 
 */
package com.aboutsip.yajpcap.framer;

import java.io.IOException;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.frame.Layer1Frame;
import com.aboutsip.yajpcap.frame.Layer2Frame;

/**
 * A layer 2 framer will frame a buffer into a layer 2 frame
 * 
 * @author jonas@jonasborjesson.com
 */
public interface Layer2Framer extends Framer<Layer1Frame> {

    @Override
    Layer2Frame frame(Layer1Frame parent, Buffer buffer) throws IOException;

}
