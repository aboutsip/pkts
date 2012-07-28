/**
 * 
 */
package com.aboutsip.yajpcap.packet;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;

/**
 * @author jonas@jonasborjesson.com
 */
public interface CSeqHeader extends SipHeader {

    Buffer NAME = Buffers.wrap("CSeq");

    Buffer getMethod();

    long getSeqNumber();

}
