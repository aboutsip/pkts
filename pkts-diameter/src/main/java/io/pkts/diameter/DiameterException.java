package io.pkts.diameter;

/**
 * @author jonas@jonasborjesson.com
 */
public class DiameterException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DiameterException() {
    }

    public DiameterException(final String message) {
        super(message);
    }

    public DiameterException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public DiameterException(final Throwable cause) {
        super(cause);
    }

}
