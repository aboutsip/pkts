/**
 *
 */
package io.pkts.gtp;

/**
 * Indicates that we expected a specific version of the GTP packet but received something else.
 *
 * @author jonas@jonasborjesson.com
 */
public class GtpVersionException extends RuntimeException {

    private final int expected;
    private final int actual;

    /**
     *
     */
    public GtpVersionException(final int expected, final int actual) {
        super(String.format("Expected GTP version %d but got %d", expected, actual));
        this.expected = expected;
        this.actual = actual;
    }

    public GtpVersionException(final int expected, final int actual, final String message) {
        super(message);
        this.expected = expected;
        this.actual = actual;
    }

    public int getExpectedVersion() {
        return expected;
    }

    public int getActualVersion() {
        return actual;
    }

}
