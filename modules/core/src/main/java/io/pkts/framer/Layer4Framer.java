/**
 * 
 */
package io.pkts.framer;

import io.pkts.buffer.Buffer;
import io.pkts.frame.Layer3Frame;
import io.pkts.frame.Layer4Frame;

import java.io.IOException;


/**
 * @author jonas@jonasborjesson.com
 */
public interface Layer4Framer extends Framer<Layer3Frame> {

    @Override
    Layer4Frame frame(Layer3Frame parent, Buffer buffer) throws IOException;

}
