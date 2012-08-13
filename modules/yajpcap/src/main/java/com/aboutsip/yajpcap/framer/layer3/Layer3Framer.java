/**
 * 
 */
package com.aboutsip.yajpcap.framer.layer3;

import java.io.IOException;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.frame.layer2.Layer2Frame;
import com.aboutsip.yajpcap.frame.layer3.Layer3Frame;
import com.aboutsip.yajpcap.framer.Framer;

/**
 * A layer 3 framer will frame a buffer into a layer 3 frame
 * 
 * @author jonas@jonasborjesson.com
 */
public interface Layer3Framer extends Framer<Layer2Frame> {

    @Override
    Layer3Frame frame(Layer2Frame parent, Buffer buffer) throws IOException;

}
