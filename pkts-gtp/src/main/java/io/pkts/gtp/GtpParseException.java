/**
 *
 */
package io.pkts.gtp;


/**
 * @author jonas@jonasborjesson.com
 */
public class GtpParseException extends GtpException {

    private final int errorOffset;
    private final String template;

    public GtpParseException(final int errorOffset, final String message) {
        super(String.format(message, errorOffset));
        this.errorOffset = errorOffset;
        template = message;
    }

    public GtpParseException(final String message) {
        this(0, message);
    }

    public GtpParseException(final int errorOffset, final String message, final Exception cause) {
        super(String.format(message, errorOffset), cause);
        this.errorOffset = errorOffset;
        template = message;
    }

    public int getErrorOffset() {
        return errorOffset;
    }

    public String getTemplate() {
        return template;
    }
}
