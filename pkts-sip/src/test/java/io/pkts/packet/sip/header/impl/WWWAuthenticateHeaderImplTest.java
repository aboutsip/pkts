package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.header.ViaHeader;
import io.pkts.packet.sip.header.WWWAuthenticateHeader;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;


public class WWWAuthenticateHeaderImplTest {

    @Test
    public void testBuild1() throws Exception {
        final WWWAuthenticateHeader wwwAuthenticateHeader = new WWWAuthenticateHeader.Builder()
                .withAlgorithm(Buffers.wrap("MD5"))
                .withNonce(Buffers.wrap("bee3366b-cf59-476e-bc5e-334e0d65b386"))
                .withQop(Buffers.wrap("auth"))
                .withRealm(Buffers.wrap("10.32.26.25"))
                .build();

        assertEquals(wwwAuthenticateHeader.getAlgorithm(), Buffers.wrap("MD5"));
        assertEquals(wwwAuthenticateHeader.getNonce(), Buffers.wrap("bee3366b-cf59-476e-bc5e-334e0d65b386"));
        assertEquals(wwwAuthenticateHeader.getQop(), Buffers.wrap("auth"));
        assertEquals(wwwAuthenticateHeader.getRealm(), Buffers.wrap("10.32.26.25"));

        Buffer value = Buffers.wrap("Digest realm=\"10.32.26.25\", nonce=\"bee3366b-cf59-476e-bc5e-334e0d65b386\", algorithm=MD5, qop=\"auth\"");
        assertTrue(wwwAuthenticateHeader.getValue().equalsIgnoreCase(value));
    }

    @Test
    public void testBuild2() throws Exception {
        final WWWAuthenticateHeader wwwAuthenticateHeader = new WWWAuthenticateHeader.Builder()
                .withNonce(Buffers.wrap("bee3366b-cf59-476e-bc5e-334e0d65b386"))
                .withRealm(Buffers.wrap("10.32.26.25"))
                .build();

        assertEquals(wwwAuthenticateHeader.getAlgorithm(), null);
        assertEquals(wwwAuthenticateHeader.getNonce(), Buffers.wrap("bee3366b-cf59-476e-bc5e-334e0d65b386"));
        assertEquals(wwwAuthenticateHeader.getQop(), null);
        assertEquals(wwwAuthenticateHeader.getRealm(), Buffers.wrap("10.32.26.25"));

        Buffer value = Buffers.wrap("Digest realm=\"10.32.26.25\", nonce=\"bee3366b-cf59-476e-bc5e-334e0d65b386\"");
        assertTrue(wwwAuthenticateHeader.getValue().equalsIgnoreCase(value));
    }

    @Test
    public void testFrame1() throws Exception {
        Buffer value = Buffers.wrap("Digest realm=\"10.32.26.25\", nonce=\"bee3366b-cf59-476e-bc5e-334e0d65b386\", algorithm=MD5, qop=\"auth\"");
        final WWWAuthenticateHeader wwwAuthenticateHeader = new WWWAuthenticateHeaderImpl(value);
        assertEquals(wwwAuthenticateHeader.getAlgorithm(), Buffers.wrap("MD5"));
        assertEquals(wwwAuthenticateHeader.getNonce(), Buffers.wrap("bee3366b-cf59-476e-bc5e-334e0d65b386"));
        assertEquals(wwwAuthenticateHeader.getQop(), Buffers.wrap("auth"));
        assertEquals(wwwAuthenticateHeader.getRealm(), Buffers.wrap("10.32.26.25"));
    }

    @Test
    public void testFrame2() throws Exception {
        Buffer realm = Buffers.wrap("10.32.26.25");
        Buffer nonce = Buffers.wrap("bee3366b-cf59-476e-bc5e-334e0d65b386");
        final WWWAuthenticateHeader wwwAuthenticateHeader = new WWWAuthenticateHeaderImpl(realm, nonce, null, null);

        assertEquals(wwwAuthenticateHeader.getAlgorithm(), null);
        assertEquals(wwwAuthenticateHeader.getNonce(), Buffers.wrap("bee3366b-cf59-476e-bc5e-334e0d65b386"));
        assertEquals(wwwAuthenticateHeader.getQop(), null);
        assertEquals(wwwAuthenticateHeader.getRealm(), Buffers.wrap("10.32.26.25"));

        Buffer value = Buffers.wrap("Digest realm=\"10.32.26.25\", nonce=\"bee3366b-cf59-476e-bc5e-334e0d65b386\"");
        assertTrue(wwwAuthenticateHeader.getValue().equalsIgnoreCase(value));
    }


}
