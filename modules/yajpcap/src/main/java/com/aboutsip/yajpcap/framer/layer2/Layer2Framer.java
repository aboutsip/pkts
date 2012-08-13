/**
 * 
 */
package com.aboutsip.yajpcap.framer.layer2;

import java.io.IOException;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.frame.layer1.Layer1Frame;
import com.aboutsip.yajpcap.frame.layer2.Layer2Frame;
import com.aboutsip.yajpcap.framer.Framer;

/**
 * A layer 2 framer will frame a buffer into a layer 2 frame
 * 
 * @author jonas@jonasborjesson.com
 */
public interface Layer2Framer extends Framer<Layer1Frame> {

    @Override
    Layer2Frame frame(Layer1Frame parent, Buffer buffer) throws IOException;

}
