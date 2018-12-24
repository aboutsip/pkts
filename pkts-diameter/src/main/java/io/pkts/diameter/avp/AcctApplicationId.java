package io.pkts.diameter.avp;

import io.pkts.diameter.avp.type.Unsigned32;

public interface AcctApplicationId extends Avp<Unsigned32> {

    int CODE = 259;

    @Override
    default long getCode() {
        return CODE;
    }
}
