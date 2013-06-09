package io.pkts.framer;

import io.pkts.buffer.Buffer;
import io.pkts.frame.Layer4Frame;
import io.pkts.frame.Layer7Frame;

import java.io.IOException;


public interface Layer7Framer extends Framer<Layer4Frame> {

    @Override
    Layer7Frame frame(Layer4Frame parent, Buffer buffer) throws IOException;

}
