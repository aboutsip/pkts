/**
 *
 */
package io.pkts.gtp;

/**
 * @author jonas@jonasborjesson.com
 */
public class GtpException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     *
     */
    public GtpException() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public GtpException(final String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public GtpException(final Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public GtpException(final String message, final Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

}
