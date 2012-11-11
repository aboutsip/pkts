/**
 * 
 */
package com.aboutsip.yajpcap.framer;

import java.io.IOException;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.frame.Layer3Frame;
import com.aboutsip.yajpcap.frame.Layer4Frame;

/**
 * @author jonas@jonasborjesson.com
 */
public interface Layer4Framer extends Framer<Layer3Frame> {

    @Override
    Layer4Frame frame(Layer3Frame parent, Buffer buffer) throws IOException;

}
