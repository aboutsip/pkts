/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.header;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;

/**
 * Represents the a content type header.
 * 
 * @author jonas@jonasborjesson.com
 */
public interface ContentTypeHeader extends SipHeader, MediaTypeHeader, Parameters {

    Buffer NAME = Buffers.wrap("Content-Type");

    ContentTypeHeader clone();

}
