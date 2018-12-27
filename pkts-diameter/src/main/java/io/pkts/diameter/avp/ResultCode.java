package io.pkts.diameter.avp;

import io.pkts.diameter.avp.impl.DiameterEnumeratedAvp;
import io.pkts.diameter.avp.type.Enumerated;
import io.pkts.diameter.avp.type.Integer32;

import java.util.Optional;

/**
 * 
 */
public interface ResultCode extends Avp<Enumerated<ResultCode.ResultCodeEnum>> {

    int CODE = 268;

    @Override
    default long getCode() {
        return CODE;
    }

    enum ResultCodeEnum {

        DIAMETER_SUCCESS("DIAMETER_SUCCESS", 2001);

        private final String name;
        private final int code;

        ResultCodeEnum(final String name, final int code) {
            this.name = name;
            this.code = code;
        }

        static Optional<ResultCodeEnum> lookup(final int code) {
            switch (code) {
                case 2001:
                    return Optional.of(DIAMETER_SUCCESS);
                default:
                    return Optional.empty();
            }
        }
    }

    default Optional<ResultCodeEnum> getAsEnum() {
        return getValue().getAsEnum();
    }

    static ResultCode parse(final FramedAvp raw) {
        if (CODE != raw.getCode()) {
            throw new AvpParseException("AVP Code mismatch - unable to parse the AVP into a " + ResultCode.class.getName());
        }
        final Integer32 value = Integer32.parse(raw.getData());
        final Optional<ResultCodeEnum> e = ResultCodeEnum.lookup(value.getValue());
        final EnumeratedHolder holder = new EnumeratedHolder(value.getValue(), e);
        return new DefaultResultCode(raw, holder);
    }

    /**
     * Ah! Must be a better way. I ran out of steam - getting late so it is what it is.
     */
    class EnumeratedHolder implements Enumerated<ResultCodeEnum> {

        private final int code;
        private final Optional<ResultCodeEnum> e;

        private EnumeratedHolder(final int code, final Optional<ResultCodeEnum> e) {
            this.code = code;
            this.e = e;
        }

        @Override
        public Optional<ResultCodeEnum> getAsEnum() {
            return e;
        }

        @Override
        public int getValue() {
            return code;
        }
    }

    class DefaultResultCode extends DiameterEnumeratedAvp<ResultCodeEnum> implements ResultCode {

        private DefaultResultCode(final FramedAvp raw, final EnumeratedHolder value) {
            super(raw, value);
        }
    }
}
