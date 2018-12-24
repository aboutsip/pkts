package io.pkts.diameter.avp;

import io.pkts.diameter.avp.impl.DiameterGroupedAvp;
import io.pkts.diameter.avp.type.Grouped;

import java.util.Optional;

public interface VendorSpecificApplicationId extends Avp<Grouped> {

    int CODE = 260;

    @Override
    default long getCode() {
        return CODE;
    }

    /**
     * The {@link VendorId}, which is a mandatory parameter.
     *
     * @return
     */
    VendorId getVendorId();

    /**
     * The {@link VendorSpecificApplicationId} must contain either a {@link AuthApplicationId} or a
     * {@link AcctApplicationId} and therefore, there are convenience methods for getting those and
     * they are returned as optionals.
     *
     * @return
     */
    Optional<AuthApplicationId> getAuthApplicationId();

    Optional<AcctApplicationId> getAcctApplicationId();

    static VendorSpecificApplicationId parse(final FramedAvp raw) {
        if (CODE != raw.getCode()) {
            throw new AvpParseException("AVP Code mismatch - unable to parse the AVP into a " + VendorSpecificApplicationId.class.getName());
        }
        return new DefaultVendorSpecificApplicationId(raw);
    }

    class DefaultVendorSpecificApplicationId extends DiameterGroupedAvp implements VendorSpecificApplicationId {
        private DefaultVendorSpecificApplicationId(final FramedAvp raw) {
            super(raw);
        }

        @Override
        public VendorId getVendorId() {
            return (VendorId) getValue().getFramedAvp(VendorId.CODE).map(FramedAvp::parse).orElse(null);
        }

        @Override
        public Optional<AuthApplicationId> getAuthApplicationId() {
            return getValue().getFramedAvp(AuthApplicationId.CODE).map(avp -> (AuthApplicationId) avp.parse());
        }

        @Override
        public Optional<AcctApplicationId> getAcctApplicationId() {
            return getValue().getFramedAvp(AcctApplicationId.CODE).map(avp -> (AcctApplicationId) avp.parse());
        }
    }


}
