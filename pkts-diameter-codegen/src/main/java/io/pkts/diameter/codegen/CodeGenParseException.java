/**
 *
 */
package io.pkts.diameter.codegen;


import org.xml.sax.Locator;

/**
 * @author jonas@jonasborjesson.com
 */
public class CodeGenParseException extends CodeGenException {

    private final int lineNo;
    private final String source;
    private final String template;

    public CodeGenParseException(final Locator locator, final String message, final Exception cause) {
        super(locator != null ? String.format(message + " (%s:%d)", locator.getSystemId(), locator.getLineNumber()) : message, cause);
        this.source = locator.getSystemId();
        this.lineNo = locator.getLineNumber();
        this.template = message;
    }

    public CodeGenParseException(final Locator locator, final String message) {
        this(locator, message, null);
    }

    public CodeGenParseException(final String message) {
        this(null, message);
    }

    public String getSource() {
        return source;
    }

    public int getLineNo() {
        return lineNo;
    }

    public String getTemplate() {
        return template;
    }
}
