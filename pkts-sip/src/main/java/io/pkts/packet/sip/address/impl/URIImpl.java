/**
 * 
 */
package io.pkts.packet.sip.address.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.address.URI;


/**
 * @author jonas@jonasborjesson.com
 */
public abstract class URIImpl implements URI {

    /**
     * All URIs are immutable and this buffer represents the "raw" URI.
     */
    private final Buffer uri;

    private final Buffer scheme;

    /**
     * 
     */
    public URIImpl(final Buffer uri, final Buffer scheme) {
        this.uri = uri;
        this.scheme = scheme;
    }

    protected Buffer getRawURI() {
        return this.uri;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getScheme() {
        return this.scheme;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSipURI() {
        return false;
    }

    @Override
    public void getBytes(final Buffer dst) {
        this.uri.getBytes(dst);
    }

    @Override
    public abstract URI clone();


}
