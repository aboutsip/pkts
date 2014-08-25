/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.header.Parameters;


/**
 * @author jonas@jonasborjesson.com
 */
public abstract class ParametersImpl extends SipHeaderImpl implements Parameters {

    private final ParametersSupport support;

    /**
     * 
     * @param name
     * @param params
     */
    protected ParametersImpl(final Buffer name, final Buffer params) {
        super(name, null);
        this.support = new ParametersSupport(params);
    }

    @Override
    public Buffer getParameter(final Buffer name) throws SipParseException {
        return this.support.getParameter(name);
    }

    @Override
    public Buffer getParameter(final String name) throws SipParseException {
        return this.support.getParameter(name);
    }

    @Override
    public Buffer setParameter(final Buffer name, final Buffer value) throws SipParseException,
    IllegalArgumentException {
        return this.support.setParameter(name, value);
    }

    /**
     * Will only return the parameters. Sub-classes will have to build up the
     * rest of the buffer {@inheritDoc}
     */
    @Override
    public Buffer getValue() {
        return this.support.toBuffer();
    }

    @Override
    protected void transferValue(final Buffer dst) {
        this.support.transferValue(dst);
    }

}
