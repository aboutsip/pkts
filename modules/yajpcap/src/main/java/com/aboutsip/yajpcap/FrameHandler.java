package com.aboutsip.yajpcap;

import com.aboutsip.yajpcap.frame.Frame;

/**
 * 
 * @author jonas@jonasborjesson.com
 */
public interface FrameHandler {

    void nextFrame(Frame frame);

}
