package io.pkts.diameter.avp;

import io.pkts.diameter.avp.impl.DiameterIdentityAvp;
import io.pkts.diameter.avp.type.DiameterIdentity;

/**
 * 
 */
public interface OriginRealm extends Avp<DiameterIdentity> {

    int CODE = 296;

    @Override
    default long getCode() {
        return CODE;
    }

    static OriginRealm parse(final FramedAvp raw) {
        if (CODE != raw.getCode()) {
            throw new AvpParseException("AVP Code mismatch - unable to parse the AVP into a " + OriginRealm.class.getName());
        }
        return new DefaultOriginRealm(raw);
    }

    class DefaultOriginRealm extends DiameterIdentityAvp implements OriginRealm {
        private DefaultOriginRealm(final FramedAvp raw) {
            super(raw);
        }
    }
}
