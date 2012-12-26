/**
 * 
 */
package com.aboutsip.streams;

import java.io.InputStream;

import org.junit.After;
import org.junit.Before;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;

/**
 * @author jonas@jonasborjesson.com
 */
public class StreamsTestBase {

    /**
     * Default stream pointing to a pcap that contains some sip traffic
     */
    protected Buffer pcapStream;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        final InputStream stream = StreamsTestBase.class.getResourceAsStream("sipp.pcap");
        this.pcapStream = Buffers.wrap(stream);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

}
