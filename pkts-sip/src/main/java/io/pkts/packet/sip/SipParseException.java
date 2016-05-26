/**
 * 
 */
package io.pkts.packet.sip;


/**
 * @author jonas@jonasborjesson.com
 */
public class SipParseException extends SipException {

    private static final long serialVersionUID = 7627471115511100108L;

    private final int errorOffset;
    private final String template;

    public SipParseException(final int errorOffset, final String message) {
        super(String.format(message, errorOffset));
        this.errorOffset = errorOffset;
        this.template = message;
    }

    public SipParseException(final String message) {
        this(0, message);
    }

    public SipParseException(final int errorOffset, final String message, final Exception cause) {
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
