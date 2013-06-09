/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.header.ContentTypeHeader;
import io.pkts.packet.sip.header.MediaTypeHeader;
import io.pkts.packet.sip.header.Parameters;
import io.pkts.packet.sip.header.impl.ContentTypeHeaderImpl;

import org.junit.Before;
import org.junit.Test;


/**
 * @author jonas@jonasborjesson.com
 * 
 */
public class ContentTypeHeaderTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * Just test a regular application/sdp media type and a few variants thereof
     */
    @Test
    public void testBasicFraming() throws Exception {
        ContentTypeHeader header = ContentTypeHeaderImpl.frame(Buffers.wrap("application/sdp"));
        assertThat(header.getContentType().toString(), is("application"));
        assertThat(header.getContentSubType().toString(), is("sdp"));
        assertThat(header.getParameter("apa"), is((Buffer) null));

        // some space around the slash is apperently ok according to rfc
        header = ContentTypeHeaderImpl.frame(Buffers.wrap("application   /sdp"));
        assertThat(header.getContentType().toString(), is("application"));
        assertThat(header.getContentSubType().toString(), is("sdp"));
        assertThat(header.getParameter("monkey"), is((Buffer) null));

        header = ContentTypeHeaderImpl.frame(Buffers.wrap("application   /   sdp"));
        assertThat(header.getContentType().toString(), is("application"));
        assertThat(header.getContentSubType().toString(), is("sdp"));
        assertThat(header.getParameter("hello"), is((Buffer) null));

        header = ContentTypeHeaderImpl.frame(Buffers.wrap("application/   sdp"));
        assertThat(header.getContentType().toString(), is("application"));
        assertThat(header.getContentSubType().toString(), is("sdp"));
        assertThat(header.getParameter("world"), is((Buffer) null));

        // some space at the end should be ok too...
        header = ContentTypeHeaderImpl.frame(Buffers.wrap("application/   sdp     "));
        assertThat(header.getContentType().toString(), is("application"));
        assertThat(header.getContentSubType().toString(), is("sdp"));
        assertThat(header.getParameter("world"), is((Buffer) null));
    }

    /**
     * Make sure that we can get out the same header again through the
     * getValue-method
     * 
     * @throws Exception
     */
    @Test
    public void testGetValue() throws Exception {
        ContentTypeHeader header = ContentTypeHeaderImpl.frame(Buffers.wrap("application/sdp"));
        assertThat(header.getValue().toString(), is("application/sdp"));

        // spaces etc will get lost in translation and that's ok i think
        header = ContentTypeHeaderImpl.frame(Buffers.wrap("application  /   sdp"));
        assertThat(header.getValue().toString(), is("application/sdp"));

        header = ContentTypeHeaderImpl.frame(Buffers.wrap("hello/world;apa=monkey"));
        assertThat(header.getValue().toString(), is("hello/world;apa=monkey"));
    }

    /**
     * Make sure we correctly identify a {@link MediaTypeHeader} of type
     * application/sdp
     * 
     * @throws Exception
     */
    @Test
    public void testIsSDP() throws Exception {
        ContentTypeHeader header = ContentTypeHeaderImpl.frame(Buffers.wrap("application/sdp"));
        assertThat(header.isSDP(), is(true));

        header = ContentTypeHeaderImpl.frame(Buffers.wrap("application/sdp   "));
        assertThat(header.isSDP(), is(true));

        // don't think you really can have anything but lower case
        // but sometimes you need to be a little forgiving and not
        // always follow the spec to 100%
        header = ContentTypeHeaderImpl.frame(Buffers.wrap("APPLICATION/SDP"));
        assertThat(header.isSDP(), is(true));

        header = ContentTypeHeaderImpl.frame(Buffers.wrap("application/SDP"));
        assertThat(header.isSDP(), is(true));

        header = ContentTypeHeaderImpl.frame(Buffers.wrap("aPPlicaTion/SDP"));
        assertThat(header.isSDP(), is(true));

        header = ContentTypeHeaderImpl.frame(Buffers.wrap("application/apa"));
        assertThat(header.isSDP(), is(false));

        header = ContentTypeHeaderImpl.frame(Buffers.wrap("hello/apa"));
        assertThat(header.isSDP(), is(false));

        header = ContentTypeHeaderImpl.frame(Buffers.wrap("hello/world"));
        assertThat(header.isSDP(), is(false));

        header = ContentTypeHeaderImpl.frame(Buffers.wrap("appli/sdp"));
        assertThat(header.isSDP(), is(false));

        header = ContentTypeHeaderImpl.frame(Buffers.wrap("bbpatcation/sdp"));
        assertThat(header.isSDP(), is(false));
    }

    /**
     * According to RFC there must be a subtype...
     * 
     * @throws Exception
     */
    @Test
    public void testNoSubType() throws Exception {
        try {
            ContentTypeHeaderImpl.frame(Buffers.wrap("missing"));
            fail("Expected a SipParseException");
        } catch (final SipParseException e) {
            // make sure we correctly identify where the problem occurs.
            assertThat(e.getErroOffset(), is(7));
        }

        try {
            ContentTypeHeaderImpl.frame(Buffers.EMPTY_BUFFER);
            fail("Expected a SipParseException");
        } catch (final SipParseException e) {
            // make sure we correctly identify where the problem occurs.
            assertThat(e.getErroOffset(), is(0));
        }

        try {
            ContentTypeHeaderImpl.frame(null);
            fail("Expected a SipParseException");
        } catch (final SipParseException e) {
            // make sure we correctly identify where the problem occurs.
            assertThat(e.getErroOffset(), is(0));
        }

        try {
            ContentTypeHeaderImpl.frame(Buffers.wrap("missing/"));
            fail("Expected a SipParseException");
        } catch (final SipParseException e) {
            // make sure we correctly identify where the problem occurs.
            assertThat(e.getErroOffset(), is(8));
        }
    }

    /**
     * The {@link Parameters} header implementation should be tested elsewhere
     * but just in case...
     * 
     * @throws Exception
     */
    @Test
    public void testWithParams() throws Exception {
        ContentTypeHeader header = ContentTypeHeaderImpl.frame(Buffers.wrap("application/   sdp     ;hello=world"));
        assertThat(header.getContentType().toString(), is("application"));
        assertThat(header.getContentSubType().toString(), is("sdp"));
        assertThat(header.getParameter("hello").toString(), is("world"));

        header = ContentTypeHeaderImpl.frame(Buffers.wrap("application/sdp;hello=world;apa=monkey"));
        assertThat(header.getParameter("hello").toString(), is("world"));
        assertThat(header.getParameter("apa").toString(), is("monkey"));

        header = ContentTypeHeaderImpl.frame(Buffers.wrap("application/sdp;flag"));
        assertThat(header.getParameter("flag").capacity(), is(0));
    }

}
