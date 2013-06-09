package io.pkts;

import io.pkts.frame.Frame;

/**
 * 
 * @author jonas@jonasborjesson.com
 */
public interface FrameHandler {

    void nextFrame(Frame frame);

}
