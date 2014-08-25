/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import static io.pkts.buffer.Buffers.wrap;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;

import org.junit.Test;

/**
 * @author jonas@jonasborjesson.com
 *
 */
public class ParametersSupportTest {

    @Test
    public void testSupportFromScratch() {
        final ParametersSupport support = new ParametersSupport(null);
        assertThat(support.getParameter("hello"), is((Buffer) null));
        assertThat(support.toBuffer().isEmpty(), is(true));
        assertThat(support.setParameter("hello", "world"), is((Buffer) null));

        // we will always write a ';' before the first parameter because
        // the primary usage of the params support is to be transfered
        // into a header or a URI etc. Hence, we do not expect people
        // to just use this class as is, in which case one probably
        // would have expected no semi-colon in the beginning of the line
        assertThat(support.toBuffer().toString(), is(";hello=world"));

        assertThat(support.setParameter("hello", "world2").toString(), is("world"));
        assertThat(support.toBuffer().toString(), is(";hello=world2"));
        assertThat(support.toBuffer().toString(), is(";hello=world2"));
        assertThat(support.getParameter("hello").toString(), is("world2"));

        assertThat(support.setParameter("foo", "boo"), is((Buffer) null));
        assertThat(support.toBuffer().toString(), is(";hello=world2;foo=boo"));
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
        assertThat(support.setParameter("transport", "udp").toString(), is("tcp"));
        assertThat(support.setParameter("hello", "world3").toString(), is("world"));
        assertThat(support.toBuffer().toString(), is(";transport=udp;lr;foo=boo;hello=world3"));

        assertThat(support.getParameter("foo").toString(), is("boo"));
        assertThat(support.setParameter("nils", "karlsson-pyssling"), is((Buffer) null));
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
