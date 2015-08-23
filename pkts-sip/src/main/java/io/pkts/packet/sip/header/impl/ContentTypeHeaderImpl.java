/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.header.ContentTypeHeader;


/**
 * @author jonas@jonasborjesson.com
 */
public final class ContentTypeHeaderImpl extends MediaTypeHeaderImpl implements ContentTypeHeader {

    /**
     * @param name
     * @param params
     */
    public ContentTypeHeaderImpl(final Buffer value, final Buffer mType, final Buffer subType, final Buffer params) {
        super(ContentTypeHeader.NAME, value, mType, subType, params);
    }

    @Override
    public ContentTypeHeader clone() {
        return new ContentTypeHeaderImpl(getValue(), getContentType(), getContentSubType(), getRawParams());
    }

    @Override
    public ContentTypeHeader.Builder copy() {
        final ContentTypeHeader.Builder builder = ContentTypeHeader.withParams(getRawParams());
        builder.withType(getContentType());
        builder.withSubType(getContentSubType());
        return builder;
    }

    @Override
    public ContentTypeHeader ensure() {
        return this;
    }
}
