
package io.pkts.diameter.avp;

import io.pkts.diameter.avp.impl.DiameterIdentityAvp;
import io.pkts.diameter.avp.type.DiameterIdentity;

/**
 * <p>
 * <b>Source: RFC6733 Section 6.3</b>
 * <pre>
 *    The Origin-Host AVP (AVP Code 264) is of type DiameterIdentity, and
 *    it MUST be present in all Diameter messages.  This AVP identifies the
 *    endpoint that originated the Diameter message.  Relay agents MUST NOT
 *    modify this AVP.
 * </pre>
 * </p>
 */
public interface OriginHost extends Avp<DiameterIdentity> {

    int CODE = 264;

    @Override
    default long getCode() {
        return CODE;
    }

    static OriginHost parse(final FramedAvp raw) {
        if (CODE != raw.getCode()) {
            throw new AvpParseException("AVP Code mismatch - unable to parse the AVP into a " + OriginHost.class.getName());
        }
        return new DefaultOriginHost(raw);
    }

    class DefaultOriginHost extends DiameterIdentityAvp implements OriginHost {
        private DefaultOriginHost(final FramedAvp raw) {
            super(raw);
        }
    }
}
