/**
 * 
 */
package com.aboutsip.yajpcap.protocol;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public final class UknownProtocolException extends Exception {

    private static final long serialVersionUID = 1L;

    private final byte code;

    public UknownProtocolException(final byte code) {
        this.code = code;
    }

}
