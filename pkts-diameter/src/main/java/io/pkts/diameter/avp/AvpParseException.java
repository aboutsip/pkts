package io.pkts.diameter.avp;

import io.pkts.diameter.DiameterParseException;

public class AvpParseException extends DiameterParseException {

    public AvpParseException(final int errorOffset, final String message) {
        super(errorOffset, message);
    }

    public AvpParseException(final String message) {
        super(message);
    }

    public AvpParseException(final int errorOffset, final String message, final Exception cause) {
        super(errorOffset, message, cause);
    }
}
