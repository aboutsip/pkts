/**
 * 
 */
package io.pkts.packet.sip.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.header.SipHeader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Tests to verify that basic parsing functionality that is provided by the
 * {@link SipParser}
 * 
 * @author jonas@jonasborjesson.com
 */
public class SipParserTest {

    protected static final String TAB = new Character('\t').toString();
    protected static final String SP = new Character(' ').toString();
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
     * The sent-protocol is the first part of a Via header, i.e. the
     * "SIP/2.0/UDP" stuff.
     * 
     * @throws Exception
     */
    @Test
    public void testConsumeSentProtocol() throws Exception {
        assertConsumeSentProtocol("SIP/2.0/UDP", "UDP", "");
        assertConsumeSentProtocol("SIP/2.0/TCP", "TCP", "");
        assertConsumeSentProtocol("SIP/2.0/TLS", "TLS", "");
        assertConsumeSentProtocol("SIP/2.0/SCTP", "SCTP", "");
        assertConsumeSentProtocol("SIP/2.0/WS", "WS", "");
        assertConsumeSentProtocol("SIP/2.0/whatever", "whatever", "");
        assertConsumeSentProtocol("SIP/2.0/UDP some left over", "UDP", " some left over");

        assertConsumeSentProtocolBadFormat("SIP/2.0UDP", "expected SipParseException because of missing slash");
        assertConsumeSentProtocolBadFormat("SIP/1.0/UDP", "expected SipParseException because of wrong version");
        assertConsumeSentProtocolBadFormat("APA/2.0/UDP", "expected SipParseException because not SIP");
        assertConsumeSentProtocolBadFormat("SIP/2.0/", "expected SipParseException because no protocol specified");
    }

    private void assertConsumeSentProtocolBadFormat(final String toParse, final String failMessage) throws Exception {
        try {
            SipParser.consumeSentProtocol(Buffers.wrap(toParse));
            fail(failMessage);
        } catch (final SipParseException e) {
            // expected
        }
    }

    private void assertConsumeSentProtocol(final String toParse, final String expectedProtocol,
            final String expectedLeftOver)
                    throws Exception {
        final Buffer buffer = Buffers.wrap(toParse);
        final Buffer protocol = SipParser.consumeSentProtocol(buffer);
        assertThat(protocol.toString(), is(expectedProtocol));
        assertThat(buffer.toString(), is(expectedLeftOver));
    }

    @Test
    public void testGetNextHeaderNameButDontCheckHColon() throws Exception {

        // space
        Buffer buffer = Buffers.wrap("hello ");
        assertThat(SipParser.nextHeaderNameDontCheckHColon(buffer).toString(), is("hello"));
        assertThat(buffer.toString(), is(" "));

        // htab
        buffer = Buffers.wrap("hello\t");
        assertThat(SipParser.nextHeaderNameDontCheckHColon(buffer).toString(), is("hello"));
        assertThat(buffer.toString(), is("\t"));

        // colon
        buffer = Buffers.wrap("hello:");
        assertThat(SipParser.nextHeaderNameDontCheckHColon(buffer).toString(), is("hello"));
        assertThat(buffer.toString(), is(":"));

        // if there is no space, htab or colon we should end up with nothing
        // and the original buffer should be left unmodified.
        buffer = Buffers.wrap("hello");
        assertThat(SipParser.nextHeaderNameDontCheckHColon(buffer), is((Buffer)null));
        assertThat(buffer.toString(), is("hello"));
    }

    @Test
    public void testConsumeUserInfoHostTest() throws Exception {
        assertConsumeUserInfoHost("127.0.0.1", null, "127.0.0.1", null, null);

        // IPv6
        assertConsumeUserInfoHost("[2001:0db8:85a3:0000:0000:8a2e:0370:7334]", null, "2001:0db8:85a3:0000:0000:8a2e:0370:7334", null, null);
        assertConsumeUserInfoHost("[2001:0DB8:85A3:0000:0000:8A2E:0370:7334]", null, "2001:0DB8:85A3:0000:0000:8A2E:0370:7334", null, null);
        assertConsumeUserInfoHost("[2001:db8::1]", null, "2001:db8::1", null, null);
        assertConsumeUserInfoHost("[::1]", null, "::1", null, null);
        assertConsumeUserInfoHost("[::]", null, "::", null, null);
        assertConsumeUserInfoHost("[:::192.168.1.1]", null, ":::192.168.1.1", null, null);
        assertConsumeUserInfoHost("[::ffff:c000:0280]", null, "::ffff:c000:0280", null, null);
        assertConsumeUserInfoHost("[::ffff:192.0.2.128]", null, "::ffff:192.0.2.128", null, null);

        // IPv6 with port
        assertConsumeUserInfoHost("[2001:0db8:85a3:0000:0000:8a2e:0370:7334]:5090", null, "2001:0db8:85a3:0000:0000:8a2e:0370:7334", "5090", null);
        assertConsumeUserInfoHost("[2001:0DB8:85A3:0000:0000:8A2E:0370:7334]:5090", null, "2001:0DB8:85A3:0000:0000:8A2E:0370:7334", "5090", null);
        assertConsumeUserInfoHost("[2001:db8::1]:5090", null, "2001:db8::1", "5090", null);
        assertConsumeUserInfoHost("[::1]:5090", null, "::1", "5090", null);
        assertConsumeUserInfoHost("[::]:5090", null, "::", "5090", null);
        assertConsumeUserInfoHost("[:::192.168.1.1]:5090", null, ":::192.168.1.1", "5090", null);
        assertConsumeUserInfoHost("[::ffff:c000:0280]:5090", null, "::ffff:c000:0280", "5090", null);
        assertConsumeUserInfoHost("[::ffff:192.0.2.128]:5090", null, "::ffff:192.0.2.128", "5090", null);

        // IPv6 with leftover
        assertConsumeUserInfoHost("[2001:0db8:85a3:0000:0000:8a2e:0370:7334];transport=tcp", null, "2001:0db8:85a3:0000:0000:8a2e:0370:7334", null, ";transport=tcp");
        assertConsumeUserInfoHost("[2001:0DB8:85A3:0000:0000:8A2E:0370:7334];transport=tcp", null, "2001:0DB8:85A3:0000:0000:8A2E:0370:7334", null, ";transport=tcp");
        assertConsumeUserInfoHost("[2001:db8::1];transport=tcp", null, "2001:db8::1", null, ";transport=tcp");
        assertConsumeUserInfoHost("[::1];transport=tcp", null, "::1", null, ";transport=tcp");
        assertConsumeUserInfoHost("[::];transport=tcp", null, "::", null, ";transport=tcp");
        assertConsumeUserInfoHost("[:::192.168.1.1];transport=tcp", null, ":::192.168.1.1", null, ";transport=tcp");
        assertConsumeUserInfoHost("[::ffff:c000:0280];transport=tcp", null, "::ffff:c000:0280", null, ";transport=tcp");
        assertConsumeUserInfoHost("[::ffff:192.0.2.128];transport=tcp", null, "::ffff:192.0.2.128", null, ";transport=tcp");

        assertConsumeUserInfoHost("alice@example.com", "alice", "example.com", null, null);
        assertConsumeUserInfoHost("alice:secret@example.com", "alice:secret", "example.com", null, null);
        assertConsumeUserInfoHost("alice@example.com:5090", "alice", "example.com", "5090", null);
        assertConsumeUserInfoHost("alice@example.com:5090;transport=tcp", "alice", "example.com", "5090",
                ";transport=tcp");
        assertConsumeUserInfoHost("example.com", null, "example.com", null, null);
        assertConsumeUserInfoHost("example.com;transport=tcp", null, "example.com", null, ";transport=tcp");
        assertConsumeUserInfoHost("example.com:9999;transport=tcp", null, "example.com", "9999", ";transport=tcp");
        assertConsumeUserInfoHost("ali;ce@example.com", "ali;ce", "example.com", null, null);
        assertConsumeUserInfoHost("ali?ce@example.com", "ali?ce", "example.com", null, null);
        assertConsumeUserInfoHost("ali?c;e@example.com", "ali?c;e", "example.com", null, null);
        assertConsumeUserInfoHost("a$&li?c;e@example.com", "a$&li?c;e", "example.com", null, null);
        assertConsumeUserInfoHost("ali;ce@example.com;transport=udp?apa=monkey", "ali;ce", "example.com", null,
                ";transport=udp?apa=monkey");
        assertConsumeUserInfoHost("example.com;transport=tcp?apa=monkey", null, "example.com", null,
                ";transport=tcp?apa=monkey");
        assertConsumeUserInfoHost("example.com?apa=monkey", null, "example.com", null, "?apa=monkey");
        assertConsumeUserInfoHost("alice@example.com?apa=monkey", "alice", "example.com", null, "?apa=monkey");
        assertConsumeUserInfoHost("al?ice@example.com?apa=monkey", "al?ice", "example.com", null, "?apa=monkey");

        // not very useful but we should parse it. Validation at a later step
        // has to determine whether this is ok or not. We are just framing
        // here...
        assertConsumeUserInfoHost("a", null, "a", null, null);

    }

    /**
     * Specifying a @ without user part is illegal.
     * 
     * @throws Exception
     */
    @Test(expected = SipParseException.class)
    public void testConsumeUserInfoHostNoUserPart() throws Exception {
        assertConsumeUserInfoHost("@a.com", "", "a.com", null, null);
    }

    /**
     * Specifying invalid host characters should throw an exception
     *
     * @throws Exception
     */
    @Test
    public void testConsumeUserInfoHostInvalidHostChars() throws Exception {
        assertConsumeUserInfoHostThrowsParseException("//customer.ip.pbx.com", 0);
        assertConsumeUserInfoHostThrowsParseException("/customer.ip.pbx.com", 0);
        assertConsumeUserInfoHostThrowsParseException("customer.ip.pbx.com/", 19);
        assertConsumeUserInfoHostThrowsParseException("customer.ip.pbx.com/test", 19);

        assertConsumeUserInfoHostThrowsParseException("", 0);
        assertConsumeUserInfoHostThrowsParseException("    ", 0);
        assertConsumeUserInfoHostThrowsParseException(".", 0);
        assertConsumeUserInfoHostThrowsParseException(".com", 0);
        assertConsumeUserInfoHostThrowsParseException("hello there!", 5);

        // Fails at index 6 because could be a valid hostname up to the domain label that did not
        // begin with a lowercase/capital letter
        assertConsumeUserInfoHostThrowsParseException("127.0.0:5060", 6);

        // Error at index 0 because entire "127" is treated as an invalid domain label
        assertConsumeUserInfoHostThrowsParseException("127:0:0:1", 0);

        assertConsumeUserInfoHostThrowsParseException("9", 0);

        // IPv6 without "[" "]"
        assertConsumeUserInfoHostThrowsParseException("2001:0db8:85a3:0000:0000:8a2e:0370:7334", 3);
        assertConsumeUserInfoHostThrowsParseException("2001:db8::1", 3);
        assertConsumeUserInfoHostThrowsParseException("::1", 0);

        // invalid IPv6
        assertConsumeUserInfoHostThrowsParseException("[::::]", 4);
        assertConsumeUserInfoHostThrowsParseException("[2001:db8:::1]", 13);
        assertConsumeUserInfoHostThrowsParseException("[2001:0db8::85a3:0000::0000:8a2e:0370:7334]", 22);
        assertConsumeUserInfoHostThrowsParseException("[2001:0db8::85a3:x000:0000:8a2e:0370:7334]", 17);
        assertConsumeUserInfoHostThrowsParseException("[:::192.168.0]", 13);
        assertConsumeUserInfoHostThrowsParseException("[:::192.168.0.4.5]", 15);
        assertConsumeUserInfoHostThrowsParseException("[:::192.168.0.1455]", 17);
        assertConsumeUserInfoHostThrowsParseException("[:::192.168.0..145]", 14);

        assertConsumeUserInfoHostThrowsParseException("customer.ip.pbx.com:", 20);
        assertConsumeUserInfoHostThrowsParseException("customer.ip.pbx.com:/", 20);

        assertConsumeUserInfoHostThrowsParseException("customer.ip.pbx.1com", 16);

        assertConsumeUserInfoHostThrowsParseException("@:;", 0);
        assertConsumeUserInfoHostThrowsParseException("@ip.pbx.com;", 0);
    }

    /**
     * Helper method for testing to extract out the user-info and host-port
     * portion of a sip uri.
     * 
     * @param toParse
     *            what to parse
     * @param expectedUser
     *            the expected user. Null if you don't expect one.
     * @param expectedHost
     *            the expected host info portion (ports and stuff will be part
     *            of this)
     * @param expectedLeftOver
     *            it is really important that we consume what we should be
     *            consuming and leave the rest alone. This is what we expect to
     *            be left after we have parsed it.
     * @throws Exception
     */
    private void assertConsumeUserInfoHost(final String toParse, final String expectedUser, final String expectedHost,
            final String expectedPort, final String expectedLeftOver)
                    throws Exception {
        final Buffer buffer = Buffers.wrap(toParse);
        final SipUserHostInfo userHost = SipParser.consumeUserInfoHostPort(buffer);
        if (expectedUser == null) {
            assertThat(userHost.getUser(), is((Buffer) null));
        } else {
            assertThat(userHost.getUser().toString(), is(expectedUser));
        }
        assertThat(userHost.getHost().toString(), is(expectedHost));
        if (expectedPort == null) {
            assertThat(userHost.getPort(), is((Buffer) null));
        } else {
            assertThat(userHost.getPort().toString(), is(expectedPort));
        }

        if (expectedLeftOver == null) {
            assertThat(buffer.hasReadableBytes(), is(false));
        } else {
            assertThat(buffer.toString(), is(expectedLeftOver));
        }
    }

    private void assertConsumeUserInfoHostThrowsParseException(final String toParse, final int expectedOffset) throws
            Exception {
        final Buffer buffer = Buffers.wrap(toParse);

        try {
            final SipUserHostInfo userHost = SipParser.consumeUserInfoHostPort(buffer);
        } catch (final SipParseException ex) {
            assertEquals(expectedOffset, ex.getErrorOffset());
            return;
        }

        fail("Expected SipParseException");
    }

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testConsumeQuotedString() throws Exception {
        Buffer buffer = Buffers.wrap("\"hello world\" fup");
        assertThat(SipParser.consumeQuotedString(buffer).toString(), is("hello world"));
        assertThat(buffer.toString(), is(" fup"));

        buffer = Buffers.wrap("\"hello world\"");
        assertThat(SipParser.consumeQuotedString(buffer).toString(), is("hello world"));
        assertThat(buffer.toString(), is(""));

        // Empty quoted string
        buffer = Buffers.wrap("\"\" <sip:hello@world.com>");
        assertThat(SipParser.consumeQuotedString(buffer).toString(), is(""));
        assertThat(buffer.toString(), is(" <sip:hello@world.com>"));

        buffer = Buffers.wrap("\"\"");
        assertThat(SipParser.consumeQuotedString(buffer).toString(), is(""));
        assertThat(buffer.toString(), is(""));

        buffer = Buffers.wrap("\"hello\"");
        assertThat(SipParser.consumeQuotedString(buffer).toString(), is("hello"));
        assertThat(buffer.toString(), is(""));

        buffer = Buffers.wrap("\"hello \\\"world\"");
        assertThat(SipParser.consumeQuotedString(buffer).toString(), is("hello \\\"world"));
        assertThat(buffer.toString(), is(""));
    }

    /**
     * Make sure that we can consume addr-spec.
     * 
     * @throws Exception
     */
    @Test
    public void testConsumeAddressSpec() throws Exception {
        Buffer buffer = Buffers.wrap("sip:alice@example.com");
        assertThat(SipParser.consumeAddressSpec(buffer).toString(), is("sip:alice@example.com"));
        assertThat(buffer.isEmpty(), is(true));

        buffer = Buffers.wrap("sip:alice@example.com>");
        assertThat(SipParser.consumeAddressSpec(buffer).toString(), is("sip:alice@example.com"));
        assertThat(buffer.toString(), is(">"));

        buffer = Buffers.wrap("sip:alice@example.com;transport=tcp");
        assertThat(SipParser.consumeAddressSpec(buffer).toString(), is("sip:alice@example.com;transport=tcp"));
        assertThat(buffer.isEmpty(), is(true));

        buffer = Buffers.wrap("sips:alice@example.com> apa");
        assertThat(SipParser.consumeAddressSpec(buffer).toString(), is("sips:alice@example.com"));
        assertThat(buffer.toString(), is("> apa"));

        buffer = Buffers.wrap("sip:alice@example.com\n");
        assertThat(SipParser.consumeAddressSpec(buffer).toString(), is("sip:alice@example.com"));
        assertThat(buffer.toString(), is("\n"));

        buffer = Buffers.wrap("whatever:alice@example.com hello");
        assertThat(SipParser.consumeAddressSpec(buffer).toString(), is("whatever:alice@example.com"));
        assertThat(buffer.toString(), is(" hello"));

        // no scheme part...
        buffer = Buffers.wrap("alice@example.com hello");
        try {
            assertThat(SipParser.consumeAddressSpec(buffer), is((Buffer) null));
            fail("Expected a SipParseException");
        } catch (final SipParseException e) {
        }

        // if we cannot find the scheme within 100 bytes then we will
        // give up...
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; ++i) {
            sb.append("a");
        }
        sb.append(":");
        buffer = Buffers.wrap(sb.toString() + "alice@example.com hello");
        try {
            assertThat(SipParser.consumeAddressSpec(buffer), is((Buffer) null));
            fail("Expected a SipParseException");
        } catch (final SipParseException e) {
        }

        // and if we haven't found the end after 1000 bytes we will also give up
        sb = new StringBuilder();
        for (int i = 0; i < SipParser.MAX_LOOK_AHEAD + 1; ++i) {
            sb.append("a");
        }
        buffer = Buffers.wrap("sip:" + sb.toString());
        try {
            assertThat(SipParser.consumeAddressSpec(buffer), is((Buffer) null));
            fail("Expected a SipParseException");
        } catch (final SipParseException e) {
        }

    }

    /**
     * <a href="https://github.com/aboutsip/pkts/issues/106">issue-106</a> related tests.
     */
    @Test
    public void testConsumeAddressSpecIssue106() throws Exception {
        ensureAddressSpecProtected("sips:alice@example.com ; hello", "sips:alice@example.com ; hello");
        ensureAddressSpecProtected("sips:alice@example.com; hello", "sips:alice@example.com; hello");
        ensureAddressSpecProtected("sips:alice@example.com;hello", "sips:alice@example.com;hello");
        ensureAddressSpecProtected("sips:alice@example.com;hello=world", "sips:alice@example.com;hello=world");
        ensureAddressSpecProtected("sips:alice@example.com;hello\t\t=world", "sips:alice@example.com;hello\t\t=world");
        ensureAddressSpecProtected("sips:alice@example.com;hello\t  \t= world", "sips:alice@example.com;hello\t  \t= world");
        ensureAddressSpecProtected("sips:alice@example.com\t\t;\thello\t  \t= world", "sips:alice@example.com\t\t;\thello\t  \t= world");
    }

    private static void ensureAddressSpecProtected(final String orig, final String expected) throws Exception {
        final Buffer buffer = Buffers.wrap(orig);
        assertThat(SipParser.consumeAddressSpec(true, buffer).toString(), is(expected));
        assertThat(buffer.toString(), is(""));
    }

    /**
     * Consuming a display name can be tricky so make sure we do it correctly.
     * 
     * @throws Exception
     */
    @Test
    public void testConsumeDisplayName() throws Exception {
        Buffer buffer = Buffers.wrap("hello <sip:alice@example.com>");
        assertThat(SipParser.consumeDisplayName(buffer).toString(), is("hello"));
        assertThat(buffer.toString(), is(" <sip:alice@example.com>")); // note that the SP should still be there

        // not actually legal but the consumeDisplayName
        // is not the one enforcing this so should work
        buffer = Buffers.wrap("hello sip:alice@example.com");
        assertThat(SipParser.consumeDisplayName(buffer).toString(), is("hello"));
        assertThat(buffer.toString(), is(" sip:alice@example.com"));

        buffer = Buffers.wrap("sip:alice@example.com");
        assertThat(SipParser.consumeDisplayName(buffer).isEmpty(), is(true));
        assertThat(buffer.toString(), is("sip:alice@example.com"));

        assertThat(SipParser.consumeDisplayName(Buffers.wrap("apa:alice@example.com")).isEmpty(), is(true));
        assertThat(SipParser.consumeDisplayName(Buffers.wrap("<sips:alice@example.com>")).isEmpty(), is(true));
        assertThat(SipParser.consumeDisplayName(Buffers.wrap("     sip:alice@example.com")).isEmpty(), is(true));

        buffer = Buffers.wrap("   <sip:alice@example.com>");
        assertThat(SipParser.consumeDisplayName(buffer).isEmpty(), is(true));
        assertThat(buffer.toString(), is("   <sip:alice@example.com>"));
    }

    /**
     * Test to consume parameters (notice the plural).
     * 
     * <pre>
     *  *( SEMI generic-param )
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void testConsumeGenericParams() throws Exception {
        assertGenericParams(";+sip.instance=\"<urn:uuid:D5E3DFFEFC3E4B69BCDFCC5DAC7BDEA9326B2EB8>\"", "+sip.instance",
                "<urn:uuid:D5E3DFFEFC3E4B69BCDFCC5DAC7BDEA9326B2EB8>");
        assertGenericParams(";a=b;c=d;foo", "a", "b", "c", "d", "foo", null);
        assertGenericParams(";a", "a", null);
        assertGenericParams(";a ;b;c = d", "a", null, "b", null, "c", "d");
        assertGenericParams("hello this is not a params");
        assertGenericParams(";lr the lr was a flag param followed by some crap", "lr", null);

        assertGenericParams("nope");
        assertGenericParams(";flag", "flag", null);
    }

    /**
     * Helper function for asserting the generic param behavior.
     * 
     * @param input
     * @param expectedKeyValuePairs
     */
    private void assertGenericParams(final String input, final String... expectedKeyValuePairs) throws Exception {
        final List<Buffer[]> params = SipParser.consumeGenericParams(Buffers.wrap(input));
        if (expectedKeyValuePairs.length == 0) {
            assertThat(params.isEmpty(), is(true));
            return;
        }

        final int noOfParams = expectedKeyValuePairs.length / 2;
        assertThat(params.size(), is(noOfParams));

        for (int i = 0; i < noOfParams; ++i) {
            final Buffer[] actual = params.get(i);
            final String expectedKey = expectedKeyValuePairs[i * 2];
            final String expectedValue = expectedKeyValuePairs[i * 2 + 1];
            assertThat(actual[0].toString(), is(expectedKey));
            if (expectedValue == null) {
                assertThat(actual[1], is((Buffer) null));
            } else {
                assertThat(actual[1].toString(), is(expectedValue));
            }
        }
    }

    /**
     * Test the following:
     * 
     * <pre>
     * generic-param  =  token [ EQUAL gen-value ]
     * gen-value      =  token / host / quoted-string
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void testConsumeGenericParam() throws Exception {
        assertGenericParam("hello=world", "hello", "world");
        assertGenericParam("hello = world", "hello", "world");
        assertGenericParam("h=w", "h", "w");

        // flag params
        assertGenericParam("hello", "hello", null);
        assertGenericParam("h", "h", null);

        // this is technically not legal according to the SIP BNF
        // but there are a lot of implementations out there that
        // does this anyway...
        assertGenericParam("h=", "h", null);

        // the SipParser.consumeGenericParam will ONLY consume
        // the first one so we should still only get hello world
        assertGenericParam("hello=world;foo=boo", "hello", "world");

        // also, it doesn't matter what comes after since it should
        // not be consumed. Off course, some of these constructs are
        // not to be found in a SIP message but these low-level parser
        // functions are not concerned about that.
        assertGenericParam("hello=world some spaces to the left", "hello", "world");

        assertGenericParam("hello = world some spaces to the left", "hello", "world");
        assertGenericParam("hello = w orld some spaces to the left", "hello", "w");
        assertGenericParam("hello = w.orld some spaces to the left", "hello", "w.orld");
    }

    /**
     * Helper function for asserting the generic param behavior.
     * 
     * @param input
     * @param expectedKey
     * @param expectedValue
     */
    private void assertGenericParam(final String input, final String expectedKey, final String expectedValue)
            throws Exception {
        final Buffer[] keyValue = SipParser.consumeGenericParam(Buffers.wrap(input));
        assertThat(keyValue[0].toString(), is(expectedKey));
        if (expectedValue == null) {
            assertThat(keyValue[1], is((Buffer) null));
        } else {
            assertThat(keyValue[1].toString(), is(expectedValue));
        }
    }

    /**
     * Make sure that we can detect alphanumerics correctly
     * 
     * @throws Exception
     */
    @Test
    public void testIsAlphaNum() throws Exception {
        for (int i = 0; i < 10; ++i) {
            assertThat(SipParser.isAlphaNum((char) ('0' + i)), is(true));
        }

        for (int i = 0; i < 26; ++i) {
            assertThat(SipParser.isAlphaNum((char) ('A' + i)), is(true));
            assertThat(SipParser.isAlphaNum((char) ('a' + i)), is(true));
        }
    }

    /**
     * Test all the below stuff
     * 
     * (from RFC 3261 25.1)
     * 
     * When tokens are used or separators are used between elements,
     * whitespace is often allowed before or after these characters:
     * 
     * STAR    =  SWS "*" SWS ; asterisk
     * SLASH   =  SWS "/" SWS ; slash
     * EQUAL   =  SWS "=" SWS ; equal
     * LPAREN  =  SWS "(" SWS ; left parenthesis
     * RPAREN  =  SWS ")" SWS ; right parenthesis
     * RAQUOT  =  ">" SWS ; right angle quote
     * LAQUOT  =  SWS "<"; left angle quote
     * COMMA   =  SWS "," SWS ; comma
     * SEMI    =  SWS ";" SWS ; semicolon
     * COLON   =  SWS ":" SWS ; colon
     * LDQUOT  =  SWS DQUOTE; open double quotation mark
     * RDQUOT  =  DQUOTE SWS ; close double quotation mark
     */
    @Test
    public void testConsumeSeparators() throws Exception {

        // basic happy testing
        assertThat(SipParser.consumeSTAR(Buffers.wrap(" * ")), is(3));
        assertThat(SipParser.consumeSTAR(Buffers.wrap("     * ")), is(7));
        assertThat(SipParser.consumeSTAR(Buffers.wrap("     *    asdf")), is(10));
        assertThat(SipParser.consumeSTAR(Buffers.wrap("*    asdf")), is(5));
        assertThat(SipParser.consumeSTAR(Buffers.wrap("*asdf")), is(1));
        assertThat(SipParser.consumeSTAR(Buffers.wrap("*")), is(1));

        assertThat(SipParser.consumeSLASH(Buffers.wrap(" / ")), is(3));
        assertThat(SipParser.consumeEQUAL(Buffers.wrap(" = ")), is(3));
        assertThat(SipParser.consumeLPAREN(Buffers.wrap(" ( ")), is(3));
        assertThat(SipParser.consumeRPAREN(Buffers.wrap(" ) ")), is(3));
        assertThat(SipParser.consumeRAQUOT(Buffers.wrap(" > ")), is(3));
        assertThat(SipParser.consumeLAQUOT(Buffers.wrap(" < ")), is(3));
        assertThat(SipParser.consumeCOMMA(Buffers.wrap(" , ")), is(3));
        assertThat(SipParser.consumeSEMI(Buffers.wrap(" ; ")), is(3));
        assertThat(SipParser.consumeCOLON(Buffers.wrap(" : ")), is(3));
        assertThat(SipParser.consumeLDQUOT(Buffers.wrap(" \"")), is(2));
        assertThat(SipParser.consumeRDQUOT(Buffers.wrap("\" ")), is(2));

        Buffer buffer = Buffers.wrap("    *    hello");
        assertThat(SipParser.consumeSTAR(buffer), is(9));
        assertThat(buffer.toString(), is("hello"));

        buffer = Buffers.wrap("\"hello\"");
        assertThat(SipParser.consumeLDQUOT(buffer), is(1));
        assertThat(SipParser.consumeToken(buffer).toString(), is("hello"));
        assertThat(SipParser.consumeRDQUOT(buffer), is(1));
    }

    /**
     * Test to consume a token as specified by RFC3261 section 25.1
     * 
     * token = 1*(alphanum / "-" / "." / "!" / "%" / "*" / "_" / "+" / "`" / "'"
     * / "~" )
     * 
     * @throws Exception
     */
    @Test
    public void testConsumeAndExpectToken() throws Exception {
        Buffer buffer = Buffers.wrap("hello world");
        assertConsumeAndExpectToken(buffer, "hello");
        SipParser.consumeWS(buffer);
        assertConsumeAndExpectToken(buffer, "world");

        buffer = Buffers.wrap("!hello");
        assertConsumeAndExpectToken(buffer, "!hello");

        final String all = "-.!%*_+`'~";
        buffer = Buffers.wrap(all + "hello");
        assertConsumeAndExpectToken(buffer, all + "hello");

        buffer = Buffers.wrap(all + "hello world" + all);
        assertConsumeAndExpectToken(buffer, all + "hello");
        SipParser.consumeWS(buffer);
        assertConsumeAndExpectToken(buffer, "world" + all);

        buffer = Buffers.wrap(all + "019hello world" + all);
        assertConsumeAndExpectToken(buffer, all + "019hello");

        buffer = Buffers.wrap("0");
        assertConsumeAndExpectToken(buffer, "0");

        buffer = Buffers.wrap("09");
        assertConsumeAndExpectToken(buffer, "09");

        buffer = Buffers.wrap("19");
        assertConsumeAndExpectToken(buffer, "19");

        buffer = Buffers.wrap("0987654321");
        assertConsumeAndExpectToken(buffer, "0987654321");

        // none of the below are part of the token "family"
        assertConsumeAndExpectToken(Buffers.wrap("&"), null);
        assertConsumeAndExpectToken(Buffers.wrap("&asdf"), null);
        assertConsumeAndExpectToken(Buffers.wrap("="), null);
        assertConsumeAndExpectToken(Buffers.wrap(";="), null);
        assertConsumeAndExpectToken(Buffers.wrap(" "), null);
        assertConsumeAndExpectToken(Buffers.wrap("\t"), null);
    }

    /**
     * Helper method that tests both consume and expect token at the same time.
     * 
     * @param buffer
     * @param expected
     */
    private void assertConsumeAndExpectToken(final Buffer buffer, final String expected) throws Exception {
        final Buffer b = buffer.slice();
        if (expected == null) {
            assertThat(SipParser.consumeToken(buffer), is((Buffer) null));
            try {
                SipParser.expectToken(b);
                fail("Expected a SipParseException because there is no token");
            } catch (final SipParseException e) {
                // expected
            }
        } else {
            assertThat(SipParser.consumeToken(buffer).toString(), is(expected));
            assertThat(SipParser.expectToken(b).toString(), is(expected));
        }

    }

    /**
     * Tests so that the index of the SipParseException is correct.
     * 
     * @throws Exception
     */
    @Test
    public void textExpectTokenSipParseException() throws Exception {
        assertSipParseExceptionIndexForExpectToken(Buffers.wrap(";hello world"), 0);
        assertSipParseExceptionIndexForExpectToken(Buffers.wrap(";"), 0);

        Buffer buffer = Buffers.wrap("hello ;world");
        SipParser.consumeToken(buffer);
        SipParser.consumeWS(buffer);
        assertSipParseExceptionIndexForExpectToken(buffer, 6);

        buffer = Buffers.wrap("hello;");
        SipParser.consumeToken(buffer);
        assertSipParseExceptionIndexForExpectToken(buffer, 5);
    }

    /**
     * Helper method for verifying the index in the {@link SipParseException}
     * for the {@link SipParser#expectToken(Buffer)}
     * 
     * @param buffer
     * @param expectedIndex
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    private void assertSipParseExceptionIndexForExpectToken(final Buffer buffer, final int expectedIndex)
            throws IndexOutOfBoundsException, IOException {
        try {
            SipParser.expectToken(buffer);
        } catch (final SipParseException e) {
            assertThat(e.getErrorOffset(), is(expectedIndex));
        }

    }

    /**
     * Make sure that we consume SEMI as defined by 3261 section 25.1
     * 
     * @throws Exception
     */
    @Test
    public void testConsumeSEMI() throws Exception {
        Buffer buffer = Buffers.wrap("  ;  hello");
        assertThat(SipParser.consumeSEMI(buffer), is(5));
        assertThat(buffer.toString(), is("hello"));

        buffer = Buffers.wrap(";  hello");
        assertThat(SipParser.consumeSEMI(buffer), is(3));
        assertThat(buffer.toString(), is("hello"));

        buffer = Buffers.wrap(";hello");
        assertThat(SipParser.consumeSEMI(buffer), is(1));
        assertThat(buffer.toString(), is("hello"));

        buffer = Buffers.wrap("hello");
        assertThat(SipParser.consumeSEMI(buffer), is(0));
        assertThat(buffer.toString(), is("hello"));

        buffer = Buffers.wrap(";");
        assertThat(SipParser.consumeSEMI(buffer), is(1));
        assertThat(buffer.toString(), is(""));
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

        assertHeader("Subject                :\r\n lunch", "Subject", "lunch");
        assertHeader("Subject:            lunch", "Subject", "lunch");
        assertHeader("Subject      :      lunch", "Subject", "lunch");
        assertHeader("Subject            :lunch", "Subject", "lunch");
        assertHeader("Subject            :      lunch", "Subject", "lunch");
        assertHeader("Subject: lunch", "Subject", "lunch");
        assertHeader("Subject   :lunch", "Subject", "lunch");
        assertHeader("Subject                :lunch", "Subject", "lunch");
    }

    /**
     * Date is a header that also allows for comma so make sure we don't parse
     * this as two headers.
     * 
     * @throws Exception
     */
    @Test
    public void testParseDateHeader() throws Exception {
        assertHeader("Date: Sun, 01 Dec 2013 18:33:36 GMT", "Date", "Sun, 01 Dec 2013 18:33:36 GMT");

        // note that these are assertHeadersSSSSSSSSS
        assertHeaders("Date: Sun, 01 Dec 2013 18:33:36 GMT", "Date", "Sun, 01 Dec 2013 18:33:36 GMT");
    }

    /**
     * The Allow header allows for comma within its value so make sure we don't
     * parse it as multiple headers.
     * 
     * @throws Exception
     */
    @Test
    public void testParseAllowHeader() throws Exception {
        assertHeader("Allow: BYE, INVITE, ACK", "Allow", "BYE, INVITE, ACK");

        // note that these are assertHeadersSSSSSSSSS
        assertHeaders("Allow: BYE, INVITE, ACK", "Allow", "BYE, INVITE, ACK");
    }

    /**
     * Even though slightly odd, it is def happening in the wild where empty headers are pushed onto
     * a message (seems like you simply shouldn't push the header to begin with, certainly will save
     * space!). When this happens, we have to make sure that we don't continue reading the next
     * header as the value of the previous empty one.
     * 
     * In the example below, the "Hello" header is empty and the value got to be the Call-ID, hence,
     * there wouldn't be any Call-ID header in the request anymore..
     * 
     * @throws Exception
     */
    @Test
    public void testEmptyHeaders() throws Exception {
        final StringBuilder sb = new StringBuilder();
        sb.append("To: <sip:jonas@127.0.0.1>\r\n");
        sb.append("Hello: \r\n");
        sb.append("Call-ID: 123641868\r\n");
        final Buffer headers = Buffers.wrap(sb.toString());
        final SipHeader to = SipParser.nextHeader(headers);
        final SipHeader hello = SipParser.nextHeader(headers);
        final SipHeader callId = SipParser.nextHeader(headers);

        assertThat(to.toString(), is("To: <sip:jonas@127.0.0.1>"));
        assertThat(hello.toString(), is("Hello: "));
        assertThat(callId.toString(), is("Call-ID: 123641868"));
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
     * Assert that we parse out all the headers as expected.
     * 
     * @param rawHeader
     *            complete raw headers.
     * @param name
     *            the expected name of the header we are looking for.
     * @param value
     *            the expected values.
     * @throws Exception
     */
    private void assertHeaders(final String rawHeader, final String name, final String... value) throws Exception {
        // remember, these headers are being framed and are therefore in a sip
        // message and as such, there will always be CRLF at the end, which is
        // why we pad them here
        final Buffer buffer = Buffers.wrap(rawHeader + "\r\n");
        final List<SipHeader> headers = SipParser.nextHeaders(buffer);
        assertThat(headers.size(), is(value.length));
        for (int i = 0; i < headers.size(); ++i) {
            final SipHeader header = headers.get(i);
            assertThat(header.getName().toString(), is(name));
            assertThat(header.getValue().toString(), is(value[i]));
        }
    }

    /**
     * Consuming a Via can be difficult since it is quite special around the
     * multiple usage of ipv6 + ipv4 addresses etc.
     * 
     * @throws Exception
     */
    @Test
    public void testConsumeVia() throws Exception {
        assertVia("SIP/2.0/UDP 127.0.0.1:5088;branch=asdf", "UDP", "127.0.0.1", "5088", null, "branch", "asdf");
        assertVia("SIP/2.0/UDP 127.0.0.1;branch=asdf", "UDP", "127.0.0.1", null, null, "branch", "asdf");
        assertVia("SIP/2.0/UDP 127.0.0.1;branch=asdf;rport", "UDP", "127.0.0.1", null, null, "branch", "asdf", "rport",
                null);
        assertVia("SIP/2.0/TCP 127.0.0.1;branch=asdf;apa=monkey", "TCP", "127.0.0.1", null, null, "branch", "asdf",
                "apa", "monkey");
        assertVia("SIP/2.0/TLS test.aboutsip.com;ttl=45;branch=asdf;apa=monkey", "TLS", "test.aboutsip.com", null,
                null, "ttl", "45", "branch", "asdf", "apa", "monkey");

        final String ipv6 = "2001:0db8:85a3:0042:1000:8a2e:0370:7334";
        assertVia("SIP/2.0/UDP " + ipv6 + ";branch=asdf", "UDP", ipv6, null, null, "branch", "asdf");
        assertVia("SIP/2.0/UDP " + ipv6 + ":9090;branch=asdf", "UDP", ipv6, "9090", null, "branch", "asdf");
        assertVia("SIP/2.0/TLS " + ipv6 + ":9090;rport;branch=asdf", "TLS", ipv6, "9090", null, "rport", null,
                "branch", "asdf");
    }

    /**
     * Check so that we detect and handle bad Via's correctly
     * 
     * @throws Exception
     */
    @Test
    public void testConsumeBadVia() throws Exception {
        assertBadVia("XML/1.0UDP 127.0.0.1:5088;branch=asdf", 1, "wrong protocol");
        assertBadVia("SIP/1.0UDP 127.0.0.1:5088;branch=asdf", 5, "wrong protocol version");
        assertBadVia("SIP/2.0UDP 127.0.0.1:5088;branch=asdf", 8, "expected to freak out on a missing slash");
        assertBadVia("SIP/2.0/UDP sip.com", 19, "no branch parameter. Should not have accepted this");
        assertBadVia("SIP/2.0/UDP :::", 15, "Strange number of colons. Cant parse a valid host out of it.");
        assertBadVia("SIP/2.0/UDP 127.0.0.1:;branch=asdf", 23, "No port specified after the colon");
    }

    /**
     * Helper method to make sure that we complain when we should.
     * 
     * @param toParse
     * @param expectedErrorOffset
     * @param failMessage
     */
    private void assertBadVia(final String toParse, final int expectedErrorOffset, final String failMessage)
            throws IOException {
        try {
            SipParser.consumeVia(Buffers.wrap(toParse));
            fail(failMessage);
        } catch (final SipParseException e) {
            assertThat(e.getErrorOffset(), is(expectedErrorOffset));
        }
    }

    /**
     * Helper method to validate the consumption of a Via header.
     * 
     * @param toParse
     *            what you want to parse. I.e. the value of the via header.
     * @param expectedProtocol
     *            the
     * @param expectedHost
     * @param expectedPort
     * @param expectedLeftOver
     *            if there is anything that should be left in the buffer.
     * @param expectedParams
     *            note, this is a String[] and you MUST supply a key value pair
     *            here. See the examples...
     * @throws Exception
     */
    private void assertVia(final String toParse, final String expectedProtocol, final String expectedHost,
            final String expectedPort, final String expectedLeftOver, final String... expectedParams) throws Exception {
        final Buffer buffer = Buffers.wrap(toParse);
        final Object[] viaParts = SipParser.consumeVia(buffer);

        assertThat(viaParts[0].toString(), is(expectedProtocol));
        assertThat(viaParts[1].toString(), is(expectedHost));
        if (expectedPort == null) {
            assertThat(viaParts[2], is((Object) null));
        } else {
            assertThat(viaParts[2].toString(), is(expectedPort));
        }

        final List<Buffer[]> actualParams = (List<Buffer[]>) viaParts[3];
        assertThat(actualParams.size(), is(expectedParams.length / 2));
        for (int i = 0; i < actualParams.size(); ++i) {
            final Buffer[] keyValue = actualParams.get(i);
            final String expectedKey = expectedParams[i * 2];
            final String expectedValue = expectedParams[i * 2 + 1];
            assertThat(keyValue[0].toString(), is(expectedKey));
            if (expectedValue == null) {
                assertThat(keyValue[1], is((Buffer) null));
            } else {
                assertThat(keyValue[1].toString(), is(expectedValue));
            }
        }

        if (expectedLeftOver == null) {
            assertThat(buffer.hasReadableBytes(), is(false));
        } else {
            assertThat(buffer.toString(), is(expectedLeftOver));
        }

    }

    /**
     * LWS expects 1 WS to be present
     * 
     * @throws Exception
     */
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

    @Test
    public void testConsumeSentBy() throws Exception {
        assertConsumeSentBy("127.0.0.1", "127.0.0.1", null, "");
        assertConsumeSentBy("aboutsip.com", "aboutsip.com", null, "");
        assertConsumeSentBy("aboutsip.com;apa=monkey", "aboutsip.com", null, ";apa=monkey");
        assertConsumeSentBy("127.0.0.1:5060;apa=monkey", "127.0.0.1", "5060", ";apa=monkey");
        assertConsumeSentBy("a:5060;apa=monkey", "a", "5060", ";apa=monkey");
    }

    private void assertConsumeSentBy(final String toParse, final String expectedHost, final String expectedPort,
            final String leftOver)
                    throws Exception {
        final Buffer buffer = Buffers.wrap(toParse);
        final Buffer[] result = SipParser.consumeSentBye(buffer);
        if (expectedHost == null) {
            assertThat(result[0], is((Buffer) null));
        } else {
            assertThat(result[0].toString(), is(expectedHost));
        }

        if (expectedPort == null) {
            assertThat(result[1], is((Buffer) null));
        } else {
            assertThat(result[1].toString(), is(expectedPort));
        }

        assertThat(buffer.toString(), is(leftOver));
    }

    @Test
    public void testConsumePort() throws Exception {
        assertThat(SipParser.consumePort(Buffers.wrap("123")).toString(), is("123"));
        assertThat(SipParser.consumePort(Buffers.wrap("0123456789")).toString(), is("0123456789"));
        assertThat(SipParser.consumePort(Buffers.wrap("1hello")).toString(), is("1"));
        assertThat(SipParser.consumePort(Buffers.wrap("hello")), is((Buffer) null));
    }

    /**
     * Make sure we consume alphanum correctly.
     */
    @Test
    public void testConsumeAlphaNum() throws Exception {
        assertConsumeAlphaNum("asdf", "asdf", "");
        assertConsumeAlphaNum("asdf123", "asdf123", "");
        assertConsumeAlphaNum("asdf123 hello", "asdf123", " hello");
        assertConsumeAlphaNum("123apa hello", "123apa", " hello");
        assertConsumeAlphaNum(" ", null, " ");
        assertConsumeAlphaNum("   space", null, "   space");
        assertConsumeAlphaNum("0123456789", "0123456789", "");
        assertConsumeAlphaNum("abcdefghiljklmnopqrstuvw", "abcdefghiljklmnopqrstuvw", "");
        assertConsumeAlphaNum("ABCDEFGHILJKLMNOPQRSTUVW", "ABCDEFGHILJKLMNOPQRSTUVW", "");
        assertConsumeAlphaNum("-", null, "-");
        assertConsumeAlphaNum("/", null, "/");
    }

    private void assertConsumeAlphaNum(final String toParse, final String expected, final String leftOver)
            throws Exception {
        final Buffer buffer = Buffers.wrap(toParse);
        final Buffer actual = SipParser.consumeAlphaNum(buffer);
        if (expected == null) {
            assertThat(actual, is((Buffer) null));
        } else {
            assertThat(actual.toString(), is(expected));
        }
        assertThat(buffer.toString(), is(leftOver));
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
        final boolean stuffConsumed = SipParser.consumeLWS(buffer) > 0;
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
        final boolean stuffConsumed = SipParser.consumeWS(buffer) > 0;
        assertThat(bufferToString(buffer), is(expected));

        // also make sure we return the boolean indicating that we consumed
        // some WS...
        assertThat(stuffConsumed, is(spBefore + tabBefore > 0));

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
        final boolean stuffConsumed = SipParser.consumeCRLF(buffer) > 0;
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

    private void assertSWSConsumption(final String SWS, final String expected, final boolean shouldWeConsumeStuff) {
        final Buffer buffer = stringToBuffer(SWS + expected);
        boolean stuffConsumed;
        try {
            stuffConsumed = SipParser.consumeSWS(buffer) > 0;
        } catch (final SipParseException e) {
            stuffConsumed = false;
        }

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
