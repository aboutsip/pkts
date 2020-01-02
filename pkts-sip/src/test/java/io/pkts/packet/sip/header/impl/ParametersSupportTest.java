/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import org.junit.Test;

import static io.pkts.buffer.Buffers.wrap;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author jonas@jonasborjesson.com
 *
 */
public class ParametersSupportTest {

    /**
     * This is part of the fix for issue 106 where too strict (actually correct according
     * to the BNF) and ultimately we ended up giving the {@link ParametersSupport} a buffer
     * that contained a ">" and due to it, it wouldn't progress. So, the too strict parsing
     * of name-addr exposed this bug here so now we are bailing out if we are not
     * making any progress while parsing the buffer.
     */
    @Test
    public void testIssueNo106() {
        final Buffer params = Buffers.wrap("wrong>");
        final ParametersSupport support = new ParametersSupport(params);
        support.getParameter("wrong");

        try {
            // this previously caused a never ending loop, now we are bailing out
            // instead.
            support.getParameter("loop");
            fail("Expected a SipParseException here");
        } catch (final SipParseException e) {
            // expected
        }
    }


    @Test
    public void testSupportFromScratch() {
        final ParametersSupport support = new ParametersSupport(null);
        assertThat(support.getParameter("hello"), is((Buffer) null));
        assertThat(support.toBuffer().isEmpty(), is(true));
        support.setParameter("hello", "world");

        // we will always write a ';' before the first parameter because
        // the primary usage of the params support is to be transfered
        // into a header or a URI etc. Hence, we do not expect people
        // to just use this class as is, in which case one probably
        // would have expected no semi-colon in the beginning of the line
        assertThat(support.toBuffer().toString(), is(";hello=world"));

        support.setParameter("hello", "world2");
        assertThat(support.toBuffer().toString(), is(";hello=world2"));
        assertThat(support.toBuffer().toString(), is(";hello=world2"));
        assertThat(support.getParameter("hello").toString(), is("world2"));

        support.setParameter("foo", "boo");
        assertThat(support.toBuffer().toString(), is(";hello=world2;foo=boo"));
    }

    @Test
    public void testTransferValue() {
        ParametersSupport support = new ParametersSupport(null);
        support.setParameter("hello", "again");
        assertTransferValue(support, ";hello=again");

        support.setParameter("hello", "again");
        assertTransferValue(support, ";hello=again");

        support.setParameter("a", "");
        support.setParameter("apa", "monkey");
        assertTransferValue(support, ";hello=again;a;apa=monkey");

        support = new ParametersSupport(wrap(";a;b=c;d"));
        assertTransferValue(support, ";a;b=c;d");
        support.setParameter("a", "");
        assertTransferValue(support, ";a;b=c;d");
        support.setParameter("a", "word");
        assertTransferValue(support, ";a=word;b=c;d");
    }

    private void assertTransferValue(final ParametersSupport support, final String expected) {
        final Buffer dst = Buffers.createBuffer(100);
        support.transferValue(dst);
        assertThat(dst.toString(), is(expected));
    }

    /**
     * We are to preserve the order in which we insert the parameters. This to guarantee that we
     * spit out the parameters in the same order we read them off the network. This includes if we
     * decide to change a parameter in the middle of what we got off the network as well.
     * 
     * @throws Exception
     */
    @Test
    public void testPreserveInsertionOrder() throws Exception {
        ParametersSupport support = new ParametersSupport(null);
        support.setParameter("foo", "boo");
        support.setParameter("hello", "world");
        assertThat(support.toBuffer().toString(), is(";foo=boo;hello=world"));

        support = new ParametersSupport(null);
        support.setParameter("hello", "world");
        support.setParameter("foo", "boo");
        assertThat(support.toBuffer().toString(), is(";hello=world;foo=boo"));

        support = new ParametersSupport(Buffers.wrap(";transport=tcp;lr;foo=boo;hello=world"));
        assertThat(support.toBuffer().toString(), is(";transport=tcp;lr;foo=boo;hello=world"));
        support.setParameter("transport", "udp");
        support.setParameter("hello", "world3");
        assertThat(support.toBuffer().toString(), is(";transport=udp;lr;foo=boo;hello=world3"));

        assertThat(support.getParameter("foo").toString(), is("boo"));
        support.setParameter("nils", "karlsson-pyssling");
        assertThat(support.toBuffer().toString(), is(";transport=udp;lr;foo=boo;hello=world3;nils=karlsson-pyssling"));
    }

    /**
     * Make sure that flag parameters are handled correctly.
     * 
     * @throws Exception
     */
    @Test
    public void testFlagParameters() throws Exception {
        ParametersSupport support = new ParametersSupport(null);
        assertFlagParam(support, wrap("lr"), null);
        assertFlagParam(support, wrap("lr"), wrap(""));
        assertFlagParam(support, wrap("lr"), Buffers.EMPTY_BUFFER);

        assertFlagParam(support, "lr", null);
        assertFlagParam(support, "lr", "");

        support.setParameter("hello", "world");
        assertThat(support.toBuffer().toString(), is(";lr;hello=world"));

        // make sure it is parsed correctly
        support = new ParametersSupport(Buffers.wrap(";transport=tcp;lr"));
        assertThat(support.getParameter("lr").isEmpty(), is(true));
        assertThat(support.getParameter("transport").toString(), is("tcp"));
    }

    /**
     * Make sure that setting, getting and "to-buffer" yields the correct for flag parameters
     * 
     * @param support
     * @param key
     * @param value
     */
    private void assertFlagParam(final ParametersSupport support, final Buffer key, final Buffer value) {
        support.setParameter(key, value);
        assertThat(support.getParameter(key).isEmpty(), is(true));
        assertThat(support.toBuffer().toString(), is(";" + key));
    }

    /**
     * Just so that the string overloaded version doesn't yeild another result.
     * 
     * @param support
     * @param key
     * @param value
     */
    private void assertFlagParam(final ParametersSupport support, final String key, final String value) {
        support.setParameter(key, value);
        assertThat(support.getParameter(key).isEmpty(), is(true));
        assertThat(support.toBuffer().toString(), is(";" + key));
    }

}
