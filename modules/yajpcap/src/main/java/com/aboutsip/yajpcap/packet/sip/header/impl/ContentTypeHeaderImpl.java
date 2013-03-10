/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.header.impl;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.packet.sip.header.ContentTypeHeader;
import com.aboutsip.yajpcap.packet.sip.impl.SipParseException;

/**
 * @author jonas@jonasborjesson.com
 */
public final class ContentTypeHeaderImpl extends MediaTypeHeaderImpl implements ContentTypeHeader {


    /**
     * @param name
     * @param params
     */
    private ContentTypeHeaderImpl(final Buffer mType, final Buffer subType, final Buffer params) {
        super(ContentTypeHeader.NAME, mType, subType, params);
    }

    /**
     * Frame the value as a {@link ContentTypeHeader}. This method assumes that
     * you have already parsed out the actual header name "Content-Type: ".
     * Also, this method assumes that a message framer (or similar) has framed
     * the buffer that is being passed in to us to only contain this header and
     * nothing else.
     * 
     * Note, as with all the frame-methods on all headers/messages/whatever,
     * they do not do any validation that the information is actually correct.
     * This method will simply only try and validate just enough to get the
     * framing done.
     * 
     * @param value
     * @return
     * @throws SipParseException
     *             in case anything goes wrong while parsing.
     */
    public static ContentTypeHeader frame(final Buffer buffer) throws SipParseException {
        final Buffer[] mediaType = MediaTypeHeaderImpl.frameMediaType(buffer);
        return new ContentTypeHeaderImpl(mediaType[0], mediaType[1], buffer);
    }

}
