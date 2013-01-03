/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.header.impl;

import java.io.IOException;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.packet.sip.address.Address;
import com.aboutsip.yajpcap.packet.sip.address.impl.AddressImpl;
import com.aboutsip.yajpcap.packet.sip.header.ToHeader;
import com.aboutsip.yajpcap.packet.sip.impl.SipParseException;
import com.aboutsip.yajpcap.packet.sip.impl.SipParser;

/**
 * @author jonas@jonasborjesson.com
 */
public final class ToHeaderImpl extends ParametersImpl implements ToHeader {

    private static final Buffer TAG = Buffers.wrap("tag");

    /**
     * The {@link Address} part of the {@link ToHeader}.
     */
    private final Address address;

    /**
     * 
     */
    private ToHeaderImpl(final Address address, final Buffer parametersBuffer) {
        super(ToHeader.NAME, parametersBuffer);
        this.address = address;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getValue() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Address getAddress() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getTag() throws SipParseException {
        return getParameter(TAG);
    }

    /**
     * Parse the value as a {@link ToHeader} value. This method assumes that you
     * have already parsed out the actual header name "To: "
     * 
     * Note, as with all the parseValue methods on all headers, they do not do
     * any validation that the information is actually correct. This method will
     * simply only try and validate just enough to get the framing done.
     * 
     * @param value
     * @return
     * @throws SipParseException
     *             in case anything goes wrong while parsing.
     */
    public static ToHeader parseValue(final Buffer buffer) throws SipParseException {
        try {
            final Address address = AddressImpl.parse(buffer);
            // final List<Buffer[]> params = SipParser.consumeGenericParams(buffer);
            final int start = buffer.getReaderIndex();
            int total = 0;
            while (buffer.hasReadableBytes() && (buffer.peekByte() == SipParser.SEMI)) {
                ++total;
                buffer.readByte();
                int count = SipParser.getTokenCount(buffer);
                buffer.readBytes(count);
                total += count;
                count = SipParser.consumeEQUAL(buffer);
                total += count;
                if (count > 0) {
                    count = SipParser.getTokenCount(buffer);
                    buffer.readBytes(count);
                    total += count;
                }
            }
            buffer.setReaderIndex(start);
            final Buffer params = buffer.readBytes(total);

            /*
            final int index = SipParser.indexOf(this.value, SipParser.SEMI);
            Buffer address = this.value;
            Buffer params = null;
            if (index > 0) {
                address = this.value.readBytes(index);
                params = this.value;
            }
            return new ToHeaderImpl(address, params);
             */
            return null;
        } catch (final IndexOutOfBoundsException e) {
            throw new SipParseException(buffer.getReaderIndex(),
                    "Unable to process the value due to a IndexOutOfBoundsException", e);
        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(),
                    "Unable to process the To-header to due an IOException");
        }
        /*
        catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(),
                    "Could not read from the underlying stream while parsing the value");
        }
         */
    }

}
