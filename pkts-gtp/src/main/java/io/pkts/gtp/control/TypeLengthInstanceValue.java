package io.pkts.gtp.control;

import io.pkts.gtp.control.impl.TypeLengthInstanceValueImpl;
import io.snice.buffer.Buffer;
import io.snice.buffer.ReadableBuffer;

/**
 * In GTPv2, all {@link InfoElement}s are of so-called TLIV - Type, Length, Instance, Value.
 */
public interface TypeLengthInstanceValue extends InfoElement {


    static TypeLengthInstanceValue frame(final Buffer buffer) {
        return TypeLengthInstanceValueImpl.frame(buffer);
    }

    static TypeLengthInstanceValue frame(final ReadableBuffer buffer) {
        return TypeLengthInstanceValueImpl.frame(buffer);
    }

    @Override
    default boolean isTypeLengthInstanceValue() {
        return true;
    }

    @Override
    default TypeLengthInstanceValue toTliv() throws ClassCastException {
        return this;
    }
}
