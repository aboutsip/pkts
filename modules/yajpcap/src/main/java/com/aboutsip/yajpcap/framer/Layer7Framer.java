package com.aboutsip.yajpcap.framer;

import java.io.IOException;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.frame.Layer4Frame;
import com.aboutsip.yajpcap.frame.Layer7Frame;

public interface Layer7Framer extends Framer<Layer4Frame> {

    @Override
    Layer7Frame frame(Layer4Frame parent, Buffer buffer) throws IOException;

}
