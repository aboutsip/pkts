/**
 * 
 */
package io.pkts.framer;

import io.pkts.buffer.Buffer;
import io.pkts.frame.Layer2Frame;
import io.pkts.frame.Layer3Frame;

import java.io.IOException;


/**
 * A layer 3 framer will frame a buffer into a layer 3 frame
 * 
 * @author jonas@jonasborjesson.com
 */
public interface Layer3Framer extends Framer<Layer2Frame> {

    @Override
    Layer3Frame frame(Layer2Frame parent, Buffer buffer) throws IOException;

}
