/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.header.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.packet.sip.SipParseException;
import com.aboutsip.yajpcap.packet.sip.header.Parameters;
import com.aboutsip.yajpcap.packet.sip.impl.SipParser;

/**
 * @author jonas@jonasborjesson.com
 */
public abstract class ParametersImpl extends SipHeaderImpl implements Parameters {

    /**
     * This buffer is the full original slice of the parameters as we received
     * them. We keep this one around since it is very common in applications
     * such as proxies etc that you only look at the parameters but never
     * actually change them so we want to keep this one around for performance
     * reasons.
     */
    private final Buffer originalParams;

    /**
     * The buffer that contains all our parameters but as we consume them, they
     * will be (well, consumed) moved over to the parameter map for fast future
     * access. Once all parameters have been consumed, this buffer will actually
     * be empty.
     */
    private final Buffer params;

    private Map<Buffer, Buffer> paramMap;

    /**
     * 
     * @param name
     * @param params
     */
    protected ParametersImpl(final Buffer name, final Buffer params) {
        super(name, null);
        if (params != null) {
            this.originalParams = params.slice();
            this.params = params;
        } else {
            this.originalParams = null;
            this.params = Buffers.EMPTY_BUFFER;
        }
    }

    @Override
    public Buffer getParameter(final Buffer name) throws SipParseException {
        if (name == null) {
            throw new IllegalArgumentException("The name of the parameter cannot be null");
        }

        if (this.paramMap != null && this.paramMap.containsKey(name)) {
            return this.paramMap.get(name);
        }

        try {
            while (this.params.hasReadableBytes()) {
                SipParser.consumeSEMI(this.params);
                final Buffer[] keyValue = SipParser.consumeGenericParam(this.params);
                if (this.paramMap == null) {
                    // default map size is 16 but params are rarely more than a few
                    this.paramMap = new HashMap<Buffer, Buffer>(8);
                }

                final Buffer value = keyValue[1] == null ? Buffers.EMPTY_BUFFER : keyValue[1];
                this.paramMap.put(keyValue[0], value);

                if (name.equals(keyValue[0])) {
                    return value;
                }
            }

            return null;
        } catch (final IndexOutOfBoundsException e) {
            throw new SipParseException(this.params.getReaderIndex(),
                    "Unable to process the value due to a IndexOutOfBoundsException", e);
        } catch (final IOException e) {
            throw new SipParseException(this.params.getReaderIndex(),
                    "Could not read from the underlying stream while parsing the value");
        }
    }

    @Override
    public Buffer getParameter(final String name) throws SipParseException {
        return getParameter(Buffers.wrap(name));
    }

    /**
     * Will only return the parameters. Sub-classes will have to build up the
     * rest of the buffer {@inheritDoc}
     */
    @Override
    public Buffer getValue() {
        // TODO: need to mark things as dirty etc in case things have been changed
        return this.originalParams;
    }

    @Override
    protected void transferValue(final Buffer dst) {
        // TODO: this is not correct
        this.originalParams.getBytes(0, dst);
    }

}
