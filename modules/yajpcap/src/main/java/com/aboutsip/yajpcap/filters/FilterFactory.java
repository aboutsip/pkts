/**
 * 
 */
package com.aboutsip.yajpcap.filters;

/**
 * @author jonas@jonasborjesson.com
 */
public final class FilterFactory {

    private static final FilterFactory instance = new FilterFactory();

    public static final FilterFactory getInstance() {
        return instance;
    }

    /**
     * 
     */
    private FilterFactory() {
        // left empty by default.
    }

    /**
     * Create a new {@link Filter}.
     * 
     * @param expression
     * @return
     */
    public Filter createFilter(final String expression) throws FilterParseException {
        if (!expression.toLowerCase().startsWith("sip.call-id")) {
            throw new FilterParseException(0, "The only valid expression at this time is sip.Call-ID");
        }

        final int index = expression.indexOf("==");
        if (index == -1) {
            throw new FilterParseException(expression.length(), "Expected a value for the sip-call-id. Missing '=='");
        }

        final String callId = expression.substring(index + 2, expression.length()).trim();
        return new SipCallIdFilter(callId);
    }

}
