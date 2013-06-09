package io.pkts.packet.sip.header;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;

/**
 * @author jonas@jonasborjesson.com
 */
public interface ContactHeader extends HeaderAddress, SipHeader, Parameters {

    Buffer NAME = Buffers.wrap("Contact");

    ContactHeader clone();

}
