package com.aboutsip.yajpcap.framer.layer7;

import java.io.IOException;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.frame.layer4.Layer4Frame;
import com.aboutsip.yajpcap.frame.layer7.Layer7Frame;
import com.aboutsip.yajpcap.framer.Framer;

public interface Layer7Framer extends Framer<Layer4Frame> {

    @Override
    Layer7Frame frame(Layer4Frame parent, Buffer buffer) throws IOException;

}
