/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.header.Parameters;

import java.util.function.Supplier;

import static io.pkts.packet.sip.impl.PreConditions.assertNotNull;


/**
 * @author jonas@jonasborjesson.com
 */
public abstract class ParametersImpl extends SipHeaderImpl implements Parameters {

    private final ParametersSupport support;
    private final Buffer params;

    /**
     * 
     * @param name
     * @param params
     */
    protected ParametersImpl(final Buffer name, final Buffer value, final Buffer params) {
        super(name, value);
        this.params = params != null ? params.slice() : Buffers.EMPTY_BUFFER;
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
    public void setParameter(final Buffer name, final Buffer value) throws SipParseException,
    IllegalArgumentException {
        this.support.setParameter(name, value);
    }

    @Override
    public void setParameter(final Buffer name, final Supplier<Buffer> value) throws SipParseException,
    IllegalArgumentException {
        assertNotNull(value);
        this.support.setParameter(name, value.get());
    }

    protected Buffer getRawParams() {
        // TODO: once buffer is truly immutable we don't need this slice stuff either.
        return this.params.slice();
    }

}
