package io.pkts.packet.sip.address.impl;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.address.TelURI;

public class TelURIImplTest {
	
	@Test
	public void testGlobalPhoneTelURI() throws SipParseException, IndexOutOfBoundsException, IOException {
		TelURI telUri = TelURI.frame(Buffers.wrap("tel:+34600100100"));
		assertEquals("34600100100", telUri.getPhoneNumber().toString());
		assertTrue(telUri.isGlobal());
	}
	
	@Test
	public void testLocalPhoneTelURI() throws SipParseException, IndexOutOfBoundsException, IOException {
		TelURI telUri = TelURI.frame(Buffers.wrap("tel:34600100100"));
		assertEquals("34600100100", telUri.getPhoneNumber().toString());
		assertFalse(telUri.isGlobal());
	}
	
	@Test(expected=SipParseException.class)
	public void testInvalidLocalPhoneTelURI() throws SipParseException, IndexOutOfBoundsException, IOException {
	    TelURI.frame(Buffers.wrap("tel:"));	   
	}
	
    
    @Test(expected=SipParseException.class)
    public void testInvalidGlobalPhoneTelURI() throws SipParseException, IndexOutOfBoundsException, IOException {
        TelURI.frame(Buffers.wrap("tel:+"));    
    }
    
    @Test(expected=SipParseException.class)
    public void testInvalidGlobalPhoneTelURIWithParams() throws SipParseException, IndexOutOfBoundsException, IOException {
        TelURI.frame(Buffers.wrap("tel:+;illegal"));    
    }
	
	@Test
	public void testGlobalPhoneTelURIWithParams() throws SipParseException, IndexOutOfBoundsException, IOException {
		TelURI telUri = TelURI.frame(Buffers.wrap("tel:+34600100100;phone-context=+34-914-555"));
		assertEquals("34600100100", telUri.getPhoneNumber().toString());
		assertEquals("+34-914-555" ,telUri.getParameter("phone-context").toString());
	}
	
	@Test
	public void testEndOfUri() throws SipParseException, IndexOutOfBoundsException, IOException {
	    Buffer buffer = Buffers.wrap("tel:+34600100100\r\nthis_is_not_teluri");
	    TelURI telUri = TelURI.frame(buffer);
	    assertEquals("34600100100", telUri.getPhoneNumber().toString());
	    assertEquals(buffer.toString(),"this_is_not_teluri");
	}
	
	@Test
    public void testBuildGlobalPhoneTelURIWithParams() throws SipParseException, IndexOutOfBoundsException, IOException {
	    TelURI telUri = TelURI.withPhoneNumber("34600200200").withGlobal(true).withParameter("ext", "111").build();
	    assertEquals("tel:+34600200200;ext=111", telUri.toString());
	}
	
	@Test
    public void testTelUriEquals() throws SipParseException, IndexOutOfBoundsException, IOException {
	    TelURI telUri = TelURI.withPhoneNumber("34600300300").withGlobal(false).withParameter("ext", "222").build();
	    assertEquals(TelURI.frame(Buffers.wrap("tel:34600300300;ext=222")), telUri);
	}
}
 