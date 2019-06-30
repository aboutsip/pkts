package io.pkts.gtp.control;

import io.snice.buffer.Buffer;

public interface InfoElement {

    byte getType();

    default int getTypeAsDecimal() {
        return Byte.toUnsignedInt(getType());
    }

    int getLength();

    Buffer getValue();

    default boolean isTypeLengthInstanceValue() {
        return false;
    }

    default TypeLengthInstanceValue toTliv() throws ClassCastException {
        throw new ClassCastException("Unable to cast a " + getClass().getName() + " into a " + TypeLengthInstanceValue.class.getName());
    }
}
