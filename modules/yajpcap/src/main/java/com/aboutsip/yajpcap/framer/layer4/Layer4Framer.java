/**
 * 
 */
package com.aboutsip.yajpcap.framer.layer4;

import java.io.IOException;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.frame.layer3.Layer3Frame;
import com.aboutsip.yajpcap.frame.layer4.Layer4Frame;
import com.aboutsip.yajpcap.framer.Framer;

/**
 * @author jonas@jonasborjesson.com
 */
public interface Layer4Framer extends Framer<Layer3Frame> {

    @Override
    Layer4Frame frame(Layer3Frame parent, Buffer buffer) throws IOException;

}
