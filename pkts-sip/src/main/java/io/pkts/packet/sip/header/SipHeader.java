/**
 * 
 */
package io.pkts.packet.sip.header;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.header.impl.SipHeaderImpl;
import io.pkts.packet.sip.impl.SipParser;

import java.io.IOException;

import static io.pkts.packet.sip.impl.PreConditions.assertNotEmpty;


/**
 * <p>
 * Represents any header in SIP.
 * </p>
 * 
 * <p>
 * All {@link SipHeader}s in this API are created through a set of factory methods as well as
 * through builders. Any header that is simple in nature, as in it only contains a single value,
 * such as the {@link MaxForwardsHeader}, can be created directly through a <code>create</code>
 * method on the the corresponding interface of that header. Headers that are constructed through
 * many arguments, or have ambiguous arguments, are created through builders. Finally, all headers
 * have a <code>frame</code> method that takes a {@link Buffer} and will attempt to frame the
 * content into a specific header. Note it <i>frames</i> the header and as such does not verify
 * every aspect of the header since speed is important.
 * </p>
 * 
 * <p>
 * Example: create a simple header directly, such as the {@link MaxForwardsHeader}.
 * 
 * <pre>
 * MaxForwardsHeader header = MaxForwardsHeader.create(20);
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * Example: create a header using the builder-pattern, such as a {@link ToHeader}.
 * 
 * <pre>
 * ToHeader header = ToHeader.with().user(&quot;alice&quot;).host(&quot;example.com&quot;).build();
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * Note, by default most things are done lazily in order to speed things up. As such, you may
 * successfully construct a header but it may in fact miss important information. If you are
 * building an io.sipstack.application.application where you want to be 100% sure that a header is correct according to the
 * BNF in rfc 3261 then call {@link #verify()}.
 * </p>
 * 
 * @author jonas@jonasborjesson.com
 */
public interface SipHeader extends Cloneable {

    /**
     * Get the name of the header
     * 
     * @return
     */
    Buffer getName();

    /**
     * Get the name as a string.
     *
     * TODO: need better method names!
     *
     * @return
     */
    default String getNameStr() {
        return getName().toString();
    }

    /**
     * Get the value of the buffer
     * 
     * @return
     */
    Buffer getValue();

    void verify() throws SipParseException;

    void getBytes(Buffer dst);

    default int getBufferSize() {
        // TODO: shouldn't be a default value. Each header should know
        return getName().capacity() + 2 + getValue().getReadableBytes();
    }


    SipHeader clone();

    /**
     * Create a new {@link SipHeader} based on the buffer. Each {@link SipHeader} will override this
     * factory method to parse the header into a more specialized header.
     * 
     * Note, the header returned really is a {@link SipHeader} and is NOT e.g. a {@link ToHeader}.
     * If you really need to parse it as a {@link ToHeader} you should use the
     * 
     * @param header the raw header
     * @return a new {@link SipHeader}.
     * @throws SipParseException in case the header is not a correct formatted header.
     */
    static SipHeader frame(final Buffer value) throws SipParseException {
        assertNotEmpty(value, "The value of the header cannot be null or the empty buffer");
        return SipParser.nextHeader(value);
    }

    static SipHeader frame(final String value) throws SipParseException {
        return SipParser.nextHeader(Buffers.wrap(value));
    }

    static SipHeader create(final String name, final String value) {
        assertNotEmpty(name, "The name of the header cannot be empty");
        return new SipHeaderImpl(Buffers.wrap(name), Buffers.wrap(value));
    }

    /**
     * As most things in this library are done lazily, such as framing headers, you can make sure
     * that a particular header has indeed been parsed to the more specific header type by calling
     * this method. If the header has yet not been parsed fully, e.g., we may have extracted out a
     * {@link ContactHeader} but it is still in its "raw" form and therefore represented as a
     * {@link SipHeader} as opposed to an actual {@link ContactHeader} but by calling this method
     * you will force the library to actually fully frame it.
     *
     * Note, if the header is successfully parsed into a more explicit header type you may
     * still not really know what to cast it so in order to make life somewhat easier you can
     * use the isXXXHeader methods (such as {@link SipHeader#isAddressParametersHeader()} to
     * check what type it possible can be and then use the corresponding toXXXHeader to
     * "cast" it.
     * 
     * @return
     */
    SipHeader ensure();

    /**
     * If you use the {@link SipHeader#ensure()} method then, if possible, the header will
     * be parsed to a more specific header type but you may not know exactly which type
     * but you can use the various isXXX methods to find out.
     * @return
     */
    default boolean isAddressParametersHeader() {
        return false;
    }

    /**
     *
     * @return
     */
    default boolean isSystemHeader() {
        return isToHeader() || isFromHeader() || isViaHeader() || isContactHeader()
                || isRouteHeader() || isRecordRouteHeader() || isCSeqHeader() || isMaxForwardsHeader();
    }

    default boolean isFromHeader() {
        final Buffer m = getName();
        try {
            if (m.getReadableBytes() == 4) {
                return m.getByte(0) == 'F' && m.getByte(1) == 'r' && m.getByte(2) == 'o'
                        && m.getByte(3) == 'm';
            } else if (m.getReadableBytes() == 1) {
                return m.getByte(0) == 'f';
            }
        } catch (final IOException e) {
            throw new SipParseException(0, "Unable to parse out the header name due to underlying IOException", e);
        }
        return false;
    }

    default FromHeader toFromHeader() {
        throw new ClassCastException("Cannot cast header of type " + getClass().getName()
                + " to type " + FromHeader.class.getName());
    }

    default boolean isToHeader() {
        final Buffer m = getName();
        try {
            if (m.getReadableBytes() == 2) {
                return m.getByte(0) == 'T' && m.getByte(1) == 'o';
            } else if (m.getReadableBytes() == 1) {
                return m.getByte(0) == 't';
            }
        } catch (final IOException e) {
            throw new SipParseException(0, "Unable to parse out the header name due to underlying IOException", e);
        }
        return false;
    }

    default ToHeader toToHeader() {
        throw new ClassCastException("Cannot cast header of type " + getClass().getName()
                + " to type " + ToHeader.class.getName());
    }

    /**
     * Check whether this is a contact header.
     *
     * Note: if this is true you MUST still do {@link SipHeader#ensure()}.{@link SipHeader#toContactHeader()}
     * because the way this method works is that if the header hasn't been fully framed
     * then the default method, which is on the {@link SipHeader} itself, will check the method name
     * if it "spells" "Contact". However, if the header you have really has been parsed into a
     * {@link ContactHeader} then this method will be overridden to return true right away
     * but since you cannot know that you must force it to be framed (if you actually want to
     * work with the {@link ContactHeader} interface i.e.), which is done by calling
     * {@link SipHeader#ensure()}.
     *
     * @return
     */
    default boolean isContactHeader() {
        final Buffer m = getName();
        try {
            if (m.getReadableBytes() == 7) {
                return m.getByte(0) == 'C' && m.getByte(1) == 'o' && m.getByte(2) == 'n'
                        && m.getByte(3) == 't' && m.getByte(4) == 'a' && m.getByte(5) == 'c'
                        && m.getByte(6) == 't';
            } else if (m.getReadableBytes() == 1) {
                // short form for the contact header is 'm'
                return m.getByte(0) == 'm';
            }
        } catch (final IOException e) {
            throw new SipParseException(0, "Unable to parse out the header name due to underlying IOException", e);
        }
        return false;
    }

    default ContactHeader toContactHeader() {
        throw new ClassCastException("Cannot cast header of type " + getClass().getName()
                + " to type " + ContactHeader.class.getName());
    }

    default boolean isCallIdHeader() {
        final Buffer m = getName();
        try {
            if (m.getReadableBytes() == 7) {
                return m.getByte(0) == 'C' && m.getByte(1) == 'a' && m.getByte(2) == 'l'
                        && m.getByte(3) == 'l' && m.getByte(4) == '-' && m.getByte(5) == 'I'
                        && m.getByte(6) == 'D';
            } else if (m.getReadableBytes() == 1) {
                // short form for the call-id header is 'i'
                return m.getByte(0) == 'i';
            }
        } catch (final IOException e) {
            throw new SipParseException(0, "Unable to parse out the header name due to underlying IOException", e);
        }
        return false;
    }

    default CallIdHeader toCallIdHeader() {
        throw new ClassCastException("Cannot cast header of type " + getClass().getName()
                + " to type " + CallIdHeader.class.getName());
    }

    default boolean isRouteHeader() {
        // Note: route header doesn't have a short form.
        final Buffer m = getName();
        if (m.getReadableBytes() == 5) {
            try {
                return m.getByte(0) == 'R' && m.getByte(1) == 'o' && m.getByte(2) == 'u'
                        && m.getByte(3) == 't' && m.getByte(4) == 'e';
            } catch (final IOException e) {
                throw new SipParseException(0, "Unable to parse out the header name due to underlying IOException", e);
            }
        }
        return false;
    }

    default boolean isRecordRouteHeader() {
        final Buffer m = getName();
        if (m.getReadableBytes() == 12) {
            try {
                return m.getByte(0) == 'R' && m.getByte(1) == 'e' && m.getByte(2) == 'c'
                        && m.getByte(3) == 'o' && m.getByte(4) == 'r' && m.getByte(5) == 'd'
                        && m.getByte(6) == '-' && m.getByte(7) == 'R' && m.getByte(8) == 'o'
                        && m.getByte(9) == 'u' && m.getByte(10) == 't' && m.getByte(11) == 'e';
            } catch (final IOException e) {
                throw new SipParseException(0, "Unable to parse out the header name due to underlying IOException", e);
            }
        }
        return false;
    }

    default RecordRouteHeader toRecordRouteHeader() {
        throw new ClassCastException("Cannot cast header of type " + getClass().getName()
                + " to type " + RecordRouteHeader.class.getName());
    }

    default RouteHeader toRouteHeader() {
        throw new ClassCastException("Cannot cast header of type " + getClass().getName()
                + " to type " + RouteHeader.class.getName());
    }

    default boolean isContentLengthHeader() {
        final Buffer m = getName();
        try {
            if (m.getReadableBytes() == 14) {
                return m.getByte(0) == 'C' && m.getByte(1) == 'o' && m.getByte(2) == 'n'
                        && m.getByte(3) == 't' && m.getByte(4) == 'e' && m.getByte(5) == 'n'
                        && m.getByte(6) == 't' && m.getByte(7) == '-' && m.getByte(8) == 'L'
                        && m.getByte(9) == 'e' && m.getByte(10) == 'n' && m.getByte(11) == 'g'
                        && m.getByte(12) == 't' && m.getByte(13) == 'h';
            } else if (m.getReadableBytes() == 1) {
                return m.getByte(0) == 'l';

            }
        } catch (final IOException e) {
            throw new SipParseException(0, "Unable to parse out the header name due to underlying IOException", e);
        }
        return false;
    }

    default ContentLengthHeader toContentLengthHeader() {
        throw new ClassCastException("Cannot cast header of type " + getClass().getName()
                + " to type " + ContentLengthHeader.class.getName());
    }

    default boolean isContentTypeHeader() {
        final Buffer m = getName();
        try {
            if (m.getReadableBytes() == 12) {
                return m.getByte(0) == 'C' && m.getByte(1) == 'o' && m.getByte(2) == 'n'
                        && m.getByte(3) == 't' && m.getByte(4) == 'e' && m.getByte(5) == 'n'
                        && m.getByte(6) == 't' && m.getByte(7) == '-' && m.getByte(8) == 'T'
                        && m.getByte(9) == 'y' && m.getByte(10) == 'p' && m.getByte(11) == 'e';
            } else if (m.getReadableBytes() == 1) {
                return m.getByte(0) == 'c';
            }
        } catch (final IOException e) {
            throw new SipParseException(0, "Unable to parse out the header name due to underlying IOException", e);
        }
        return false;
    }

    default ContentTypeHeader toContentTypeHeader() {
        throw new ClassCastException("Cannot cast header of type " + getClass().getName()
                + " to type " + ContentTypeHeader.class.getName());
    }

    default boolean isExpiresHeader() {
        final Buffer m = getName();
        try {
            if (m.getReadableBytes() == 7) {
                return m.getByte(0) == 'E' && m.getByte(1) == 'x' && m.getByte(2) == 'p'
                        && m.getByte(3) == 'i' && m.getByte(4) == 'r' && m.getByte(5) == 'e'
                        && m.getByte(6) == 's';
            }
        } catch (final IOException e) {
            throw new SipParseException(0, "Unable to parse out the header name due to underlying IOException", e);
        }
        return false;
    }

    default ExpiresHeader toExpiresHeader() {
        throw new ClassCastException("Cannot cast header of type " + getClass().getName()
                + " to type " + ExpiresHeader.class.getName());
    }

    default boolean isCSeqHeader() {
        final Buffer m = getName();
        try {
            if (m.getReadableBytes() == 4) {
                return m.getByte(0) == 'C' && m.getByte(1) == 'S' && m.getByte(2) == 'e'
                        && m.getByte(3) == 'q';
            }
        } catch (final IOException e) {
            throw new SipParseException(0, "Unable to parse out the header name due to underlying IOException", e);
        }
        return false;
    }

    default CSeqHeader toCSeqHeader() {
        throw new ClassCastException("Cannot cast header of type " + getClass().getName()
                + " to type " + CSeqHeader.class.getName());
    }

    default boolean isMaxForwardsHeader() {
        final Buffer m = getName();
        if (m.getReadableBytes() == 12) {
            try {
                return m.getByte(0) == 'M' && m.getByte(1) == 'a' && m.getByte(2) == 'x'
                        && m.getByte(3) == '-' && m.getByte(4) == 'F' && m.getByte(5) == 'o'
                        && m.getByte(6) == 'r' && m.getByte(7) == 'w' && m.getByte(8) == 'a'
                        && m.getByte(9) == 'r' && m.getByte(10) == 'd' && m.getByte(11) == 's';
            } catch (final IOException e) {
                throw new SipParseException(0, "Unable to parse out the header name due to underlying IOException", e);
            }
        }
        return false;
    }

    default ViaHeader toViaHeader() {
        throw new ClassCastException("Cannot cast header of type " + getClass().getName()
                + " to type " + ViaHeader.class.getName());
    }

    default boolean isViaHeader() {
        final Buffer m = getName();
        try {
            if (m.getReadableBytes() == 3) {
                return m.getByte(0) == 'V' && m.getByte(1) == 'i' && m.getByte(2) == 'a';
            } else if (m.getReadableBytes() == 1) {
                // Short form for the via header is a 'v'
                return m.getByte(0) == 'v';
            }
        } catch (final IOException e) {
            throw new SipParseException(0, "Unable to parse out the header name due to underlying IOException", e);
        }
        return false;
    }

    default MaxForwardsHeader toMaxForwardsHeader() {
        throw new ClassCastException("Cannot cast header of type " + getClass().getName()
                + " to type " + MaxForwardsHeader.class.getName());
    }

    default AddressParametersHeader toAddressParametersHeader() throws ClassCastException {
        throw new ClassCastException("Cannot cast header of type " + getClass().getName()
                + " to type " + AddressParametersHeader.class.getName());
    }

    /**
     * Everything within the pkts.io SIP module are immutable so if you actually want
     * to change anything you have to create a copy, which will return a specific Builder
     * for that header.
     *
     * @return
     */
    Builder copy();

    interface Builder<H extends SipHeader> {

        Builder<H> withValue(Buffer value);

        default Builder<H> withValue(final String value) {
            return withValue(Buffers.wrap(value));
        }

        H build() throws SipParseException;
    }


}
