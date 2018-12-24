package io.pkts.diameter.avp.type;

import io.pkts.buffer.Buffer;

public interface Integer64 extends DiameterType {

    DiameterType.Type TYPE = Type.INTEGER_64;

    static Integer64 parse(final Buffer data) {
        return new DefaultInteger64(data.getUnsignedInt(data.getReaderIndex()));
    }

    long getValue();

    class DefaultInteger64 implements Integer64 {
        private final long value;

        private DefaultInteger64(final long value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }
    }
}
