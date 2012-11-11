/**
 * 
 */
package com.aboutsip.yajpcap.packet.impl.sip;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.packet.sip.SipHeader;
import com.aboutsip.yajpcap.packet.sip.impl.SipParseException;
import com.aboutsip.yajpcap.packet.sip.impl.SipParser;

/**
 * Tests to verify that basic parsing functionality that is provided by the
 * {@link SipParser}
 * 
 * @author jonas@jonasborjesson.com
 */
public class SipParserTest {

    protected static final String TAB = (new Character('\t')).toString();
    protected static final String SP = (new Character(' ')).toString();
    protected static final String CRLF = "\r\n";
    protected static final String CR = "\r";
    protected static final String LF = "\r";

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Make sure we recognize and bail out on a non-digit
     * 
     * @throws Exception
     */
    @Test
    public void testExpectDigitFailure() throws Exception {
        try {
            SipParser.expectDigit(Buffers.wrap("abc"));
            fail("Expected SipParseException");
        } catch (final SipParseException e) {
        }

        try {
            // character '/' is just before zero in the ascii table so
            // therefore some boundary testing
            SipParser.expectDigit(Buffers.wrap("/abc"));
            fail("Expected SipParseException");
        } catch (final SipParseException e) {
        }

        try {
            // character ':' is just after 9 in the ascii table so
            // therefore some boundary testing
            SipParser.expectDigit(Buffers.wrap(":abc"));
            fail("Expected SipParseException");
        } catch (final SipParseException e) {
        }

        try {
            SipParser.expectDigit(Buffers.wrap("    "));
            fail("Expected SipParseException");
        } catch (final SipParseException e) {
        }
    }

    @Test
    public void testExpectDigit() throws Exception {
        assertThat(SipParser.expectDigit(Buffers.wrap("213 apa")).toString(), is("213"));
        assertThat(SipParser.expectDigit(Buffers.wrap("2 apa")).toString(), is("2"));
        assertThat(SipParser.expectDigit(Buffers.wrap("2apa")).toString(), is("2"));
        assertThat(SipParser.expectDigit(Buffers.wrap("2")).toString(), is("2"));
        assertThat(SipParser.expectDigit(Buffers.wrap("0")).toString(), is("0"));
        assertThat(SipParser.expectDigit(Buffers.wrap("9")).toString(), is("9"));
        assertThat(SipParser.expectDigit(Buffers.wrap("9   ")).toString(), is("9"));
    }

    /**
     * Taken from section 7.3.1 Header Field Format in RFC3261
     * 
     * @throws Exception
     */
    @Test
    public void testLunch() throws Exception {

        assertHeader("Subject:            lunch", "Subject", "lunch");
        assertHeader("Subject      :      lunch", "Subject", "lunch");
        assertHeader("Subject            :lunch", "Subject", "lunch");
        assertHeader("Subject            :      lunch", "Subject", "lunch");
        assertHeader("Subject: lunch", "Subject", "lunch");
        assertHeader("Subject   :lunch", "Subject", "lunch");
        assertHeader("Subject                :lunch", "Subject", "lunch");
        assertHeader("Subject                :\r\n lunch", "Subject", "lunch");
    }

    /**
     * Test so that we actually can handle folded lines correctly...
     * 
     * @throws Exception
     */
    @Test
    public void testFoldedHeader() throws Exception {
        final String expectedValue = "I know you're there, pick up the phone and talk to me!";
        final String foldedValue = "I know you're there,\r\n" + "      pick up the phone\r\n" + TAB + "and talk to me!";

        assertHeader("Subject: " + foldedValue + "\r\n", "Subject", expectedValue);
    }

    private void assertHeader(final String rawHeader, final String name, final String value) throws Exception {
        // remember, these headers are being framed and are therefore in a sip
        // message and as such, there will always be CRLF at the end, which is
        // why we pad them here
        final Buffer buffer = Buffers.wrap(rawHeader + "\r\n");
        final SipHeader header = SipParser.nextHeader(buffer);
        assertThat(header.getName().toString(), is(name));
        assertThat(header.getValue().toString(), is(value));
    }

    /**
     * LWS expects 1 WS to be present
     * 
     * @throws Exception
     */
    @Test(expected = SipParseException.class)
    public void testConsumeLWSBad1() throws Exception {
        assertLWSConsumption("", "monkey");
    }

    /**
     * LWS expects 1 WS to be present after a CRLF
     * 
     * @throws Exception
     */
    @Test(expected = SipParseException.class)
    public void testConsumeLWSBad2() throws Exception {
        assertLWSConsumption(CRLF, "monkey");
    }

    @Test(expected = SipParseException.class)
    public void testConsumeLWSBad3() throws Exception {
        assertLWSConsumption(TAB + SP + CRLF, "monkey");
    }

    /**
     * Make sure that we can consume LWS according to spec (even though we do
     * consume a little too much WS in certain cases, see comment in test and in
     * code)
     * 
     * @throws Exception
     */
    @Test
    public void testConsumeLWS() throws Exception {
        assertLWSConsumption(" ", "monkey");
        assertLWSConsumption(TAB, "monkey");

        // LWS expects 1 SP to be present after a CRLF
        assertLWSConsumption(CRLF + SP, "monkey");
        assertLWSConsumption(CRLF + TAB, "monkey");

        // many WS followed by one CRLF followed by one WS is ok
        assertLWSConsumption(TAB + SP + CRLF + SP, "monkey");

        // this is not quite according to spec since we really
        // aren't supposed to consume extra WS if there is no CRLF present
        // However, for now let's keep it like this...
        assertLWSConsumption(TAB + SP + SP, "monkey");
        assertLWSConsumption(SP + SP, "monkey");
        assertLWSConsumption(SP + SP + TAB + TAB, "monkey");
    }

    private void assertLWSConsumption(final String LWS, final String expected) throws Exception {
        final Buffer buffer = stringToBuffer(LWS + expected);
        final boolean stuffConsumed = SipParser.consumeLWS(buffer);
        assertThat(bufferToString(buffer), is(expected));

        // in the case of LWS, we should always consume at least 1 WS
        // otherwise there should have been an exception thrown
        assertThat(stuffConsumed, is(true));
    }

    @Test(expected = SipParseException.class)
    public void testExpectWSButNoWS() throws Exception {
        SipParser.expectWS(stringToBuffer("no ws here!"));
    }

    @Test
    public void testExpectWS() throws Exception {
        Buffer buffer = stringToBuffer(" hello");
        SipParser.expectWS(buffer);
        assertThat(bufferToString(buffer), is("hello"));

        buffer = stringToBuffer(TAB + "hello");
        SipParser.expectWS(buffer);
        assertThat(bufferToString(buffer), is("hello"));

        try {
            buffer = stringToBuffer("hello");
            SipParser.expectWS(buffer);
            fail("expected ParseException here due to no WS, which was expected");
        } catch (final SipParseException e) {
            // this is important though (we is why we are doing the
            // old style junit3 fail thingie

            // the buffer should still be at "hello" since we did not
            // extract out any white space
            assertThat(bufferToString(buffer), is("hello"));
        }

    }

    @Test
    public void testConsumeWhitespace() throws Exception {
        assertWSConsumption(0, 0, "hello");
        assertWSConsumption(1, 0, "hello");
        assertWSConsumption(0, 1, "hello");
        assertWSConsumption(1, 1, "hello");
        assertWSConsumption(1, 1, "hello ");
        assertWSConsumption(2, 2, "hello ");
        assertWSConsumption(20, 2, "hello whatever more ws at the end         ");
    }

    private void assertWSConsumption(final int spBefore, final int tabBefore, final String expected)
            throws SipParseException {
        String padding = "";
        for (int i = 0; i < spBefore; ++i) {
            padding += SP;
        }

        for (int i = 0; i < tabBefore; ++i) {
            padding += TAB;
        }

        final Buffer buffer = stringToBuffer(padding + expected);
        final boolean stuffConsumed = SipParser.consumeWS(buffer);
        assertThat(bufferToString(buffer), is(expected));

        // also make sure we return the boolean indicating that we consumed
        // some WS...
        assertThat(stuffConsumed, is((spBefore + tabBefore) > 0));

    }

    @Test
    public void testReadUntilCRLF() throws Exception {
        assertReadUntilCRLF("hello ", " and this is the stuff that should be left");
        assertReadUntilCRLF("", "Having the line start with CRLF should work too");
        assertReadUntilCRLF("", "");
    }

    private void assertReadUntilCRLF(final String whatsRead, final String whatsLeft) throws Exception {
        final Buffer buffer = stringToBuffer(whatsRead + "\r\n" + whatsLeft);
        final Buffer result = buffer.readLine();
        assertThat(bufferToString(result), is(whatsRead));
        assertThat(bufferToString(buffer), is(whatsLeft));
    }

    @Test
    public void testConsumeCRLF() throws Exception {
        assertCRLFConsumption(CRLF + "hello", "hello", true);

        assertCRLFConsumption(LF + "hello", LF + "hello", false);
        assertCRLFConsumption(CR + "hello", CR + "hello", false);
        assertCRLFConsumption(SP + CRLF + "hello", SP + CRLF + "hello", false);
    }

    private void assertCRLFConsumption(final String parse, final String expected, final boolean expectedConsumption)
            throws SipParseException {
        final Buffer buffer = stringToBuffer(parse);
        final boolean stuffConsumed = SipParser.consumeCRLF(buffer);
        assertThat(bufferToString(buffer), is(expected));
        assertThat(stuffConsumed, is(expectedConsumption));
    }

    /**
     * The difference between SWS and LWS is that SWS has LWS as optional.
     * Hence, there should never be any exceptions thrown out of SWS
     * 
     * @throws Exception
     */
    @Test
    public void testConsumeSWS() throws Exception {

        // same as from the LWS and the underlying implementation
        // is actually the same but from a test perspective we dont
        // "know" that so just make sure it works for SWS as well.
        // Just in case we ever change the underlying implementation
        assertSWSConsumption(" ", "monkey", true);
        assertSWSConsumption(TAB, "monkey", true);
        assertSWSConsumption(CRLF + SP, "monkey", true);
        assertSWSConsumption(CRLF + TAB, "monkey", true);
        assertSWSConsumption(TAB + SP + CRLF + SP, "monkey", true);
        assertSWSConsumption(TAB + SP + SP, "monkey", true);
        assertSWSConsumption(SP + SP, "monkey", true);
        assertSWSConsumption(SP + SP + TAB + TAB, "monkey", true);

        // the following would have blown up in the case of LWS
        // hmmm, not sure about in particularly the last one. Is this really
        // what people do?
        assertSWSConsumption("", "monkey", false);
        assertSWSConsumption(CRLF, "monkey", false);
        assertSWSConsumption(TAB + SP + CRLF, "monkey", false);

    }

    private void assertSWSConsumption(final String SWS, final String expected, final boolean shouldWeConsumeStuff)
            throws Exception {
        final Buffer buffer = stringToBuffer(SWS + expected);
        final boolean stuffConsumed = SipParser.consumeSWS(buffer);
        assertThat(bufferToString(buffer), is(expected));

        assertThat(stuffConsumed, is(shouldWeConsumeStuff));
    }

    public String bufferToString(final Buffer buffer) {
        return buffer.toString();
    }

    public Buffer stringToBuffer(final String s) {
        return Buffers.wrap(s.getBytes());
    }

}
