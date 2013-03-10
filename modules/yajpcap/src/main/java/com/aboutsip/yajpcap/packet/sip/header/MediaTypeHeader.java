/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.header;

import com.aboutsip.buffer.Buffer;

/**
 * Interface for those headers representing a media type, such as the
 * {@link ContentTypeHeader}
 * 
 * @author jonas@jonasborjesson.com
 */
public interface MediaTypeHeader {

    /**
     * 
     * @return
     */
    Buffer getContentType();

    /**
     * 
     * @return
     */
    Buffer getContentSubType();

    /**
     * Convenience method for checking whether the media type is
     * "application/sdp"
     * 
     * @return
     */
    boolean isSDP();

}
