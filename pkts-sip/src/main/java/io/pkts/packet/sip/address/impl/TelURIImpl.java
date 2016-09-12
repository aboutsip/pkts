package io.pkts.packet.sip.address.impl;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.address.TelURI;
import io.pkts.packet.sip.address.URI;
import io.pkts.packet.sip.header.impl.ParametersSupport;
import io.pkts.packet.sip.impl.SipParser;

/**
 * 
 * @author dmnava
 *
 */
public class TelURIImpl extends URIImpl implements TelURI {

    private final boolean isGlobal;
    private final Buffer phoneNumber;
    private final Buffer headers;
    private final ParametersSupport paramsSupport;

    public TelURIImpl(final boolean isGlobal, final Buffer phoneNumber, final Buffer headers, final Buffer original) {
        super(original, SipParser.SCHEME_TEL);
        this.isGlobal = isGlobal;
        this.phoneNumber = phoneNumber;
        this.headers = headers.slice();
        this.paramsSupport = new ParametersSupport(headers);
    }

    @Override
    public URI clone() {
        return new TelURIImpl(isGlobal, phoneNumber, headers, getRawURI());
    }

    @Override
    public TelURI.Builder copy() {
        return TelURI.withPhoneNumber(phoneNumber)
                .withGlobal(isGlobal)
                .withParameters(paramsSupport);
    }

    @Override
    public boolean isGlobal() {
        return isGlobal;
    }

    @Override
    public Buffer getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public Buffer getParameter(final Buffer name) throws SipParseException, IllegalArgumentException {
        return this.paramsSupport.getParameter(name);
    }

    @Override
    public Buffer getParameter(final String name) throws SipParseException, IllegalArgumentException {
        return this.paramsSupport.getParameter(name);
    }

    @Override
    public Buffer toBuffer() {
        return getRawURI().slice();
    }

    @Override
    public String toString() {
        return toBuffer().toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((headers == null) ? 0 : headers.hashCode());
        result = prime * result + (isGlobal ? 1231 : 1237);
        result = prime * result + ((phoneNumber == null) ? 0 : phoneNumber.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof TelURIImpl) {
            TelURIImpl other = (TelURIImpl) obj;
            if (isGlobal != other.isGlobal || !phoneNumber.toString().equals(other.getPhoneNumber().toString())) {
                return false;
            }
            if (this.paramsSupport != null && other.paramsSupport != null) {
                final Set<Map.Entry<Buffer, Buffer>> entries = this.paramsSupport.getAllParameters();
                if (entries != null) {
                    final Iterator<Map.Entry<Buffer, Buffer>> it = entries.iterator();
                    while (it.hasNext()) {
                        final Map.Entry<Buffer, Buffer> entry = it.next();
                        final Buffer key = entry.getKey();
                        final Buffer value = entry.getValue();
                        final Buffer bValue = other.getParameter(key);
                        if (other.paramsSupport.hasParameter(key)) {
                            if (value == null ^ bValue == null) {
                                return false;
                            }
                            if (!value.equalsIgnoreCase(bValue)) {
                                return false;
                            }
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

}
