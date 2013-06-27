/**
 * 
 */
package io.pkts.packet.sip.header;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;

/**
 * Represents the a content type header.
 * 
 * @author jonas@jonasborjesson.com
 */
public interface ContentTypeHeader extends SipHeader, MediaTypeHeader, Parameters {

    Buffer NAME = Buffers.wrap("Content-Type");

    ContentTypeHeader clone();

}
