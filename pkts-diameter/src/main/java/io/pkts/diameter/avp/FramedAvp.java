package io.pkts.diameter.avp;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.ReadOnlyBuffer;
import io.pkts.diameter.DiameterParseException;
import io.pkts.diameter.impl.DiameterParser;

import java.io.IOException;

/**
 * A {@link FramedAvp} is an AVP who has not been fully parsed, i.e., you only have access to the {@link AvpHeader}
 * and the raw data. However, if you want to convert it to a known type, you can call the {@link FramedAvp#parse()}
 * method to actually parse the structure into, hopefully, a known AVP.
 */
public interface FramedAvp {

    static FramedAvp frame(final ReadOnlyBuffer buffer) throws DiameterParseException, IOException {
        return DiameterParser.frameRawAvp(buffer);
    }

    /**
     * Return the amount of padding needed for this AVP.
     *
     * @return
     */
    int getPadding();

    AvpHeader getHeader();


    /**
     * Convenience method for getting the AVP code from the {@link AvpHeader}
     *
     * @return
     */
    default long getCode() {
        return getHeader().getCode();
    }

    Buffer getData();

    /**
     * Fully parse this raw AVP to something known. If the AVP isn't known,
     * then you'll get back a unknown AVP, which is really just the same as the
     * {@link FramedAvp} and then you have to figure things out for yourself.
     *
     * @return
     */
    Avp parse();
}
