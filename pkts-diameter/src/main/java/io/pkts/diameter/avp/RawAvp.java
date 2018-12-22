package io.pkts.diameter.avp;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.ReadOnlyBuffer;
import io.pkts.diameter.DiameterParseException;
import io.pkts.diameter.impl.DiameterParser;

import java.io.IOException;

/**
 * A {@link RawAvp} is an AVP who has not been fully framed, i.e., you only have access to the {@link AvpHeader}
 * and the raw data. However, if you want to convert it to a known type, you can call the
 */
public interface RawAvp {

    static RawAvp frame(final ReadOnlyBuffer buffer) throws DiameterParseException, IOException {
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
     * {@link RawAvp} and then you have to figure things out for yourself.
     *
     * @return
     */
    Avp parse();

    /**
     * Since every diameter message must have a origin host, it is one of the
     * most used AVPs and therefore this convenience method.
     *
     * @return true if this {@link RawAvp} is {@link OriginHost}
     */
    default boolean isOriginHost() {
        return OriginHost.CODE == getCode();
    }

    default OriginHost toOriginHost() {
        return OriginHost.parse(this);
    }

    default boolean isOriginRealm() {
        return OriginRealm.CODE == getCode();
    }

    default OriginRealm toOriginRealm() {
        return OriginRealm.parse(this);
    }
}
