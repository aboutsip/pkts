package io.pkts.diameter.avp.type;

import io.pkts.buffer.Buffer;

import java.util.Optional;

public interface Enumerated<T extends Enum<T>> extends Integer32 {

    static <T extends Enum<T>> Enumerated<T> parse(final Buffer data) {
        final int code = data.getInt(data.getReaderIndex());
        return new DefaultEnumerated(code);
    }

    /**
     * If the value corresponds to a known Enum, it will be returned. However, not everything
     * is well specified and in those cases you will get back an empty {@link Optional}.
     *
     * @return
     */
    Optional<T> getAsEnum();

    class DefaultEnumerated<T extends Enum<T>> implements Enumerated<T> {
        private final int value;

        private DefaultEnumerated(final int value) {
            this.value = value;
        }

        @Override
        public int getValue() {
            return value;
        }

        @Override
        public Optional<T> getAsEnum() {
            return null;
        }
    }
}
