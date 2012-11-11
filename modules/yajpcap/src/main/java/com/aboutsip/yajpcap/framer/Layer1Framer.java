/**
 * 
 */
package com.aboutsip.yajpcap.framer;

import java.io.IOException;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.frame.Frame;
import com.aboutsip.yajpcap.frame.Layer1Frame;

/**
 * @author jonas@jonasborjesson.com
 */
public interface Layer1Framer extends Framer<Frame> {

    @Override
    Layer1Frame frame(Frame parent, Buffer buffer) throws IOException;
}
