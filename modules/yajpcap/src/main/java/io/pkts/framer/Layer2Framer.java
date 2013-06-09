/**
 * 
 */
package io.pkts.framer;

import io.pkts.buffer.Buffer;
import io.pkts.frame.Layer1Frame;
import io.pkts.frame.Layer2Frame;

import java.io.IOException;


/**
 * A layer 2 framer will frame a buffer into a layer 2 frame
 * 
 * @author jonas@jonasborjesson.com
 */
public interface Layer2Framer extends Framer<Layer1Frame> {

    @Override
    Layer2Frame frame(Layer1Frame parent, Buffer buffer) throws IOException;

}
