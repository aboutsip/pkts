/**
 * 
 */
package io.pkts.packet.diameter;


/**
 * @author jonas@jonasborjesson.com
 */
public class DiameterParseException extends DiameterException {

    private final int errorOffset;
    private final String template;

    public DiameterParseException(final int errorOffset, final String message) {
        super(String.format(message, errorOffset));
        this.errorOffset = errorOffset;
        this.template = message;
    }

    public DiameterParseException(final String message) {
        this(0, message);
    }

    public DiameterParseException(final int errorOffset, final String message, final Exception cause) {
        super(String.format(message, errorOffset), cause);
        this.errorOffset = errorOffset;
        this.template = message;
    }

    public int getErrorOffset() {
        return this.errorOffset;
    }

    public String getTemplate() {
        return template;
    }
}
