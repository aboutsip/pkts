package io.pkts.diameter.avp;

import io.pkts.diameter.avp.impl.DiameterGroupedAvp;
import io.pkts.diameter.avp.type.Grouped;

public interface VendorSpecificApplicationId extends Avp<Grouped> {

    int CODE = 260;

    @Override
    default long getCode() {
        return CODE;
    }

    static VendorSpecificApplicationId parse(final RawAvp raw) {
        if (CODE != raw.getCode()) {
            throw new AvpParseException("AVP Code mismatch - unable to parse the AVP into a " + VendorSpecificApplicationId.class.getName());
        }
        return new DefaultVendorSpecificApplicationId(raw);
    }

    class DefaultVendorSpecificApplicationId extends DiameterGroupedAvp implements VendorSpecificApplicationId {
        private DefaultVendorSpecificApplicationId(final RawAvp raw) {
            super(raw);
        }
    }


}
