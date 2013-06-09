/**
 * 
 */
package io.pkts.framer;

import io.pkts.buffer.Buffer;
import io.pkts.frame.Frame;
import io.pkts.frame.Layer1Frame;

import java.io.IOException;


/**
 * @author jonas@jonasborjesson.com
 */
public interface Layer1Framer extends Framer<Frame> {

    @Override
    Layer1Frame frame(Frame parent, Buffer buffer) throws IOException;
}
