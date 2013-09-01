/**
 * 
 */
package io.pkts.packet.sip.header;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;

/**
 * @author jonas@jonasborjesson.com
 */
public interface CSeqHeader extends SipHeader {

    Buffer NAME = Buffers.wrap("CSeq");

    Buffer getMethod();

    long getSeqNumber();

    CSeqHeader clone();

}
