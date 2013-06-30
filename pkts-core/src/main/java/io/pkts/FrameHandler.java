package io.pkts;

import io.pkts.frame.Frame;

import java.io.IOException;

/**
 * 
 * @author jonas@jonasborjesson.com
 */
public interface FrameHandler {

    void nextFrame(Frame frame) throws IOException;

}
