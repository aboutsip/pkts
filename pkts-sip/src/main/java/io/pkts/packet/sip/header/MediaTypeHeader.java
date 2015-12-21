/**
 * 
 */
package io.pkts.packet.sip.header;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.header.impl.ParametersSupport;
import io.pkts.packet.sip.impl.SipParser;

import static io.pkts.packet.sip.impl.PreConditions.assertNotEmpty;

/**
 * Interface for those headers representing a media type, such as the
 * {@link ContentTypeHeader}
 * 
 * @author jonas@jonasborjesson.com
 */
public interface MediaTypeHeader extends SipHeader {

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
     * "io.sipstack.application.application/sdp"
     * 
     * @return
     */
    boolean isSDP();


    /**
     * Convenience method for parsing out a media type header.
     * 
     * @param buffer
     * @return
     * @throws SipParseException
     */
    static Buffer[] frame(final Buffer buffer) throws SipParseException {
        if (buffer == null) {
            throw new SipParseException(0, "Cannot parse a null-buffer. Cmon!");
        }

        final Buffer mType = SipParser.consumeMType(buffer);
        if (mType == null) {
            throw new SipParseException(buffer.getReaderIndex(), "Expected m-type but got nothing");
        }
        SipParser.expectSLASH(buffer);
        final Buffer subType = SipParser.consumeMSubtype(buffer);
        if (subType == null) {
            throw new SipParseException(buffer.getReaderIndex(), "Expected m-subtype but got nothing");
        }
        return new Buffer[] {mType, subType};
    }

    abstract class Builder<T extends MediaTypeHeader> implements SipHeader.Builder<T> {

        private final Buffer name;

        private ParametersSupport paramSupport;

        private Buffer type;

        private Buffer subType;

        protected Builder(final Buffer name) {
            this(name, null);
        }

        protected Builder(final Buffer name, final Buffer params) {
            this.name = name;
            this.paramSupport = new ParametersSupport(params);
        }

        /**
         * Set a parameter on the header.
         *
         * @param name
         * @param value
         * @return
         * @throws SipParseException
         * @throws IllegalArgumentException
         */
        public Builder<T> withParameter(final Buffer name, final Buffer value) throws SipParseException,
                IllegalArgumentException {
            this.paramSupport.setParameter(name, value);
            return this;
        }

        public Builder<T> withParameter(final String name, final String value) throws SipParseException,
                IllegalArgumentException {
            this.paramSupport.setParameter(name, value);
            return this;
        }

        /**
         * Set a bunch of parameters at the same time.
         *
         * WARNING! This one will wipe out any previously set parameters so be sure to call
         * this one FIRST before any other calls to withParameter...
         *
         * @param params
         * @return
         */
        public Builder<T> withParameters(final Buffer params) {
            this.paramSupport = new ParametersSupport(params);
            return this;
        }

        /**
         * Remove all header parameters.
         *
         * @return
         */
        public Builder<T> withNoParameters() {
            this.paramSupport = new ParametersSupport();
            return this;
        }

        /**
         * Set the "main" content type, i.e. if you try and create a media type of
         * "application/sdp" then this would be the "application" portion and the
         * sub-type is "sdp".
         *
         * @param type
         * @return
         */
        public final Builder<T> withType(final Buffer type) {
            this.type = type;
            return this;
        }

        public final Builder<T> withType(final String type) {
            return withType(Buffers.wrap(type));
        }

        /**
         * Set the sub-section of the media type. I.e., if you try and create a
         * media type of "application/sdp" then the subtype would be "sdp".
         *
         * @param subType
         * @return
         */
        public final Builder<T> withSubType(final Buffer subType) {
            this.subType = subType;
            return this;
        }

        public final Builder<T> withSubType(final String subType) {
            return withSubType(Buffers.wrap(subType));
        }

        @Override
        public final T build() throws SipParseException {
            assertNotEmpty(type, "The content type cannot be null or empty");
            assertNotEmpty(subType, "The content sub-type cannot be null or empty");
            final Buffer params = this.paramSupport.toBuffer();

            // build up the final value
            final int size = type.capacity() + 1 + subType.capacity() + params.capacity();
            final Buffer value = Buffers.createBuffer(size);
            type.getBytes(0, value);
            value.write(SipParser.SLASH);
            subType.getBytes(0, value);
            params.getBytes(0, value);
            return internalBuild(value, type, subType, params);
        }

        abstract protected T internalBuild(final Buffer rawValue,
                                           final Buffer type,
                                           final Buffer subType,
                                           final Buffer params);
    }

}
