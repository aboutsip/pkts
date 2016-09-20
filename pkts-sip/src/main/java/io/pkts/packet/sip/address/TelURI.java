package io.pkts.packet.sip.address;

import static io.pkts.packet.sip.impl.PreConditions.assertNotEmpty;
import static io.pkts.packet.sip.impl.PreConditions.assertNotNull;

import java.io.IOException;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.address.impl.TelURIImpl;
import io.pkts.packet.sip.header.impl.ParametersSupport;
import io.pkts.packet.sip.impl.PreConditions;
import io.pkts.packet.sip.impl.SipParser;
import io.pkts.packet.sip.impl.TelUriParser;

/**
 * 
 * @author dmnava
 *
 */
public interface TelURI extends URI {

    /**
     * Specifies whether the telephone number is global or not
     * 
     * @return
     */
    boolean isGlobal();

    /**
     * Returns the phone number, without the '+' in case of global number
     * 
     * @return
     */
    Buffer getPhoneNumber();

    @Override
    default boolean isTelURI() {
        return true;
    }

    @Override
    default TelURI toTelURI() {
        return this;
    }

    /**
     * Get the value of the named parameter. If the named parameter is a
     * so-called flag parameter, then the value returned will be an empty
     * {@link Buffer}, which can be checked with {@link Buffer#isEmpty()} or
     * {@link Buffer#capacity()}, which will return zero. As with any empty
     * {@link Buffer}, if you do {@link Buffer#toString()} you will be getting
     * an empty {@link String} back, which would be yet another way to check for
     * a flag parameter.
     * 
     * @param name
     *            the name of the parameter we are looking for.
     * @return the value of the named parameter or null if there is no such
     *         parameter. If the named parameter is a flag parameter, then an
     *         empty buffer will be returned.
     * @throws SipParseException
     *             in case anything goes wrong while extracting the parameter.
     * @throws IllegalArgumentException
     *             in case the name is null.
     */
    Buffer getParameter(Buffer name) throws SipParseException, IllegalArgumentException;

    /**
     * Same as {@link #getParameter(Buffer)}.
     * 
     * @param name
     * @return
     * @throws SipParseException
     *             in case anything goes wrong while extracting the parameter.
     * @throws IllegalArgumentException
     *             in case the name is null.
     */
    Buffer getParameter(String name) throws SipParseException, IllegalArgumentException;

    /**
     * Frame a TEL Uri, which according to RFC 3966 has the following syntax:
     * 
     * <pre>
     *    telephone-uri        = "tel:" telephone-subscriber
     *    telephone-subscriber = global-number / local-number
     *    global-number        = global-number-digits *par
     *    local-number         = local-number-digits *par context *par
     *    par                  = parameter / extension / isdn-subaddress
     *    isdn-subaddress      = ";isub=" 1*uric
     *    extension            = ";ext=" 1*phonedigit
     *    context              = ";phone-context=" descriptor
     *    descriptor           = domainname / global-number-digits
     *    global-number-digits = "+" *phonedigit DIGIT *phonedigit
     *    local-number-digits  = phonedigit-hex (HEXDIG / "*" / "#")*phonedigit-hex
     *    domainname           = *( domainlabel "." ) toplabel [ "." ]
     *    domainlabel          = alphanum
     *                              / alphanum *( alphanum / "-" ) alphanum
     *    toplabel             = ALPHA / ALPHA *( alphanum / "-" ) alphanum
     *    parameter            = ";" pname ["=" pvalue ]
     *    pname                = 1*( alphanum / "-" )
     *    pvalue               = 1*paramchar
     *    paramchar            = param-unreserved / unreserved / pct-encoded
     *    unreserved           = alphanum / mark
     *    mark                 = "-" / "_" / "." / "!" / "~" / "*" / "'" / "(" / ")"
     *    pct-encoded          = "%" HEXDIG HEXDIG
     *    param-unreserved     = "[" / "]" / "/" / ":" / "&" / "+" / "$"
     *    phonedigit           = DIGIT / [ visual-separator ]
     *    phonedigit-hex       = HEXDIG / "*" / "#" / [ visual-separator ]
     *    visual-separator     = "-" / "." / "(" / ")"
     *    alphanum             = ALPHA / DIGIT
     *    reserved             = ";" / "/" / "?" / ":" / "@" / "&" / "=" / "+" / "$" / ","
     *    uric                 = reserved / unreserved / pct-encoded
     * </pre>
     * 
     * 
     * @param buffer
     * @return
     * @throws SipParseException
     * @throws IndexOutOfBoundsException
     * @throws IOException
     */
    static TelURI frame(final Buffer buffer) throws SipParseException, IndexOutOfBoundsException, IOException {
        final Buffer original = buffer.slice();
        try {
            SipParser.expectTel(buffer);
        } catch (final SipParseException e) {
            throw new SipParseException(e.getErrorOffset() - 1, "TEL URI must start with tel:");
        }

        TelUriParser parser = new TelUriParser(buffer, original);
        return parser.getTelUri();
    }
    
    static Builder withPhoneNumber(final Buffer phoneNumber) {
        final Builder builder = new Builder();
        return builder.withPhoneNumber(phoneNumber);
    }
    
    
    static Builder withPhoneNumber(final String phoneNumber) {
        final Builder builder = new Builder();
        return builder.withPhoneNumber(phoneNumber);
    }
    
    
    class Builder extends URI.Builder<TelURI> {
        
        private boolean isGlobal;
        private Buffer phoneNumber;
        private ParametersSupport paramSupport = new ParametersSupport();
        
        public Builder withPhoneNumber(Buffer phoneNumber) {
            assertNotNull(phoneNumber, "phoneNumber cannot be null");
            this.phoneNumber = phoneNumber;
            return this;
        }
        
        public Builder withPhoneNumber(String phoneNumber) {
            assertNotEmpty(phoneNumber, "phoneNumber cannot be null or the empty string");
            return withPhoneNumber(Buffers.wrap(phoneNumber));
        }
        
        public Builder withGlobal(final boolean isGlobal) {
            this.isGlobal = isGlobal;
            return this;
        }
        
        public Builder withParameter(final Buffer name, final Buffer value) throws SipParseException,
        IllegalArgumentException {
            this.paramSupport.setParameter(name, value);
            return this;
        }

        public Builder withParameter(final String name, final String value) throws SipParseException,
        IllegalArgumentException {
            this.paramSupport.setParameter(name, value);
            return this;
        }

        public Builder withParameter(final String name, final int value) throws SipParseException,
                IllegalArgumentException {
            this.paramSupport.setParameter(Buffers.wrap(name), Buffers.wrap(value));
            return this;
        }

        public Builder withParameter(final Buffer name, final int value) throws SipParseException,
                IllegalArgumentException {
            this.paramSupport.setParameter(name, Buffers.wrap(value));
            return this;
        }

        public Builder withNoParameters() {
            this.paramSupport = new ParametersSupport(null);
            return this;
        }
        
        public Builder withParameters(ParametersSupport paramSupport) {
            this.paramSupport = paramSupport;
            return this;
        }
        
        @Override
        public TelURI build() throws SipParseException {
            PreConditions.assertNotEmpty(this.phoneNumber, "Phone number cannot be empty");
            int size = 4; // tel:
            size += isGlobal ? 1 : 0;
            size += phoneNumber.capacity();
            final Buffer params = this.paramSupport.toBuffer();
            if (params != null) {
                size += params.capacity();
            }
            
            final Buffer uri = Buffers.createBuffer(size);
            SipParser.SCHEME_TEL_COLON.getBytes(0, uri);
            if (isGlobal)
                uri.write(SipParser.PLUS);
            phoneNumber.getBytes(0, uri);
            params.getBytes(0, uri);
            
            return new TelURIImpl(isGlobal, phoneNumber, params, uri);
        }


    }

     
    
}
