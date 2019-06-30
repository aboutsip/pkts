package io.pkts.gtp.control.impl;

import io.pkts.gtp.GtpHeader;
import io.pkts.gtp.control.Gtp2Header;
import io.pkts.gtp.control.Gtp2Message;
import io.pkts.gtp.control.InfoElement;
import io.pkts.gtp.control.TypeLengthInstanceValue;
import io.snice.buffer.Buffer;
import io.snice.buffer.ReadableBuffer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.snice.preconditions.PreConditions.assertNotNull;

public class Gtp2MessageImpl implements Gtp2Message {

    private final Gtp2Header header;
    private final Buffer body;
    private final List<TypeLengthInstanceValue> values;

    public static Gtp2Message frame(final Gtp2Header header, final ReadableBuffer buffer) {
        assertNotNull(header, "The GTPv2 header cannot be null");
        assertNotNull(buffer, "The buffer cannot be null");
        final Buffer body = buffer.readBytes(header.getBodyLength());

        final ReadableBuffer values = body.toReadableBuffer();
        final List<TypeLengthInstanceValue> tlivs = new ArrayList<>(); // TODO: what's a good default value?
        while (values.hasReadableBytes()) {
            final TypeLengthInstanceValue tliv = TypeLengthInstanceValue.frame(values);
            tlivs.add(tliv);
        }

        return new Gtp2MessageImpl(header, body, Collections.unmodifiableList(tlivs));
    }

    private Gtp2MessageImpl(final Gtp2Header header, final Buffer body, final List<TypeLengthInstanceValue> values) {
        this.header = header;
        this.body = body;
        this.values = values;
    }

    @Override
    public GtpHeader getHeader() {
        return header;
    }

    @Override
    public List<? extends InfoElement> getInfoElements() {
        return values;
    }
}
