/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.header.ContentTypeHeader;
import io.pkts.packet.sip.header.MediaTypeHeader;
import io.pkts.packet.sip.header.Parameters;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

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

    @Test
    public void testCopyConstructor() {
        final ContentTypeHeader h1 = ContentTypeHeader.withType("application").withSubType("apa").build();
        final ContentTypeHeader h2 = h1.copy().withSubType("nisse").build();
        final ContentTypeHeader h3 = h2.copy().withSubType("nisse+json").withParameter("apa", "monkey").build();
        final ContentTypeHeader h4 = h3.copy().withType("hello").build();
        final ContentTypeHeader h5 = h3.copy().withNoParameters().build();

        assertThat(h1.toString(), is("Content-Type: application/apa"));
        assertThat(h2.toString(), is("Content-Type: application/nisse"));
        assertThat(h3.toString(), is("Content-Type: application/nisse+json;apa=monkey"));
        assertThat(h4.toString(), is("Content-Type: hello/nisse+json;apa=monkey"));
        assertThat(h5.toString(), is("Content-Type: application/nisse+json"));

        assertThat(h1.getValue().toString(), is("application/apa"));
        assertThat(h2.getValue().toString(), is("application/nisse"));
        assertThat(h3.getValue().toString(), is("application/nisse+json;apa=monkey"));
        assertThat(h4.getValue().toString(), is("hello/nisse+json;apa=monkey"));
        assertThat(h5.getValue().toString(), is("application/nisse+json"));
    }


    /**
     * Just test a regular application/sdp media type and a few variants thereof
     */
    @Test
    public void testBasicFraming() throws Exception {
        ContentTypeHeader header = ContentTypeHeader.frame(Buffers.wrap("application/sdp"));
        assertThat(header.getContentType().toString(), is("application"));
        assertThat(header.getContentSubType().toString(), is("sdp"));
        assertThat(header.getParameter("apa"), is((Buffer) null));

        // some space around the slash is apperently ok according to rfc
        header = ContentTypeHeader.frame(Buffers.wrap("application   /sdp"));
        assertThat(header.getContentType().toString(), is("application"));
        assertThat(header.getContentSubType().toString(), is("sdp"));
        assertThat(header.getParameter("monkey"), is((Buffer) null));

        header = ContentTypeHeader.frame(Buffers.wrap("application   /   sdp"));
        assertThat(header.getContentType().toString(), is("application"));
        assertThat(header.getContentSubType().toString(), is("sdp"));
        assertThat(header.getParameter("hello"), is((Buffer) null));

        header = ContentTypeHeader.frame(Buffers.wrap("application/   sdp"));
        assertThat(header.getContentType().toString(), is("application"));
        assertThat(header.getContentSubType().toString(), is("sdp"));
        assertThat(header.getParameter("world"), is((Buffer) null));

        // some space at the end should be ok too...
        header = ContentTypeHeader.frame(Buffers.wrap("application/   sdp     "));
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
        ContentTypeHeader header = ContentTypeHeader.frame(Buffers.wrap("application/sdp"));
        assertThat(header.getValue().toString(), is("application/sdp"));

        header = ContentTypeHeader.frame(Buffers.wrap("application  /   sdp"));
        assertThat(header.getValue().toString(), is("application  /   sdp"));

        header = ContentTypeHeader.frame(Buffers.wrap("hello/world;apa=monkey"));
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
        ContentTypeHeader header = ContentTypeHeader.frame(Buffers.wrap("application/sdp"));
        assertThat(header.isSDP(), is(true));

        header = ContentTypeHeader.frame(Buffers.wrap("application/sdp   "));
        assertThat(header.isSDP(), is(true));

        // don't think you really can have anything but lower case
        // but sometimes you need to be a little forgiving and not
        // always follow the spec to 100%
        header = ContentTypeHeader.frame(Buffers.wrap("APPLICATION/SDP"));
        assertThat(header.isSDP(), is(true));

        header = ContentTypeHeader.frame(Buffers.wrap("application/SDP"));
        assertThat(header.isSDP(), is(true));

        header = ContentTypeHeader.frame(Buffers.wrap("aPPlicaTion/SDP"));
        assertThat(header.isSDP(), is(true));

        header = ContentTypeHeader.frame(Buffers.wrap("application/apa"));
        assertThat(header.isSDP(), is(false));

        header = ContentTypeHeader.frame(Buffers.wrap("hello/apa"));
        assertThat(header.isSDP(), is(false));

        header = ContentTypeHeader.frame(Buffers.wrap("hello/world"));
        assertThat(header.isSDP(), is(false));

        header = ContentTypeHeader.frame(Buffers.wrap("appli/sdp"));
        assertThat(header.isSDP(), is(false));

        header = ContentTypeHeader.frame(Buffers.wrap("bbpatcation/sdp"));
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
            ContentTypeHeader.frame(Buffers.wrap("missing"));
            fail("Expected a SipParseException");
        } catch (final SipParseException e) {
            // make sure we correctly identify where the problem occurs.
            assertThat(e.getErrorOffset(), is(7));
        }

        try {
            ContentTypeHeader.frame(Buffers.EMPTY_BUFFER);
            fail("Expected a SipParseException");
        } catch (final SipParseException e) {
            // make sure we correctly identify where the problem occurs.
            assertThat(e.getErrorOffset(), is(0));
        }

        try {
            ContentTypeHeader.frame(null);
            fail("Expected a SipParseException");
        } catch (final SipParseException e) {
            // make sure we correctly identify where the problem occurs.
            assertThat(e.getErrorOffset(), is(0));
        }

        try {
            ContentTypeHeader.frame(Buffers.wrap("missing/"));
            fail("Expected a SipParseException");
        } catch (final SipParseException e) {
            // make sure we correctly identify where the problem occurs.
            assertThat(e.getErrorOffset(), is(8));
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
        ContentTypeHeader header = ContentTypeHeader.frame(Buffers.wrap("application/   sdp     ;hello=world"));
        assertThat(header.getContentType().toString(), is("application"));
        assertThat(header.getContentSubType().toString(), is("sdp"));
        assertThat(header.getParameter("hello").toString(), is("world"));

        header = ContentTypeHeader.frame(Buffers.wrap("application/sdp;hello=world;apa=monkey"));
        assertThat(header.getParameter("hello").toString(), is("world"));
        assertThat(header.getParameter("apa").toString(), is("monkey"));

        header = ContentTypeHeader.frame(Buffers.wrap("application/sdp;flag"));
        assertThat(header.getParameter("flag").capacity(), is(0));
    }

}
