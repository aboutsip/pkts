/**
 * 
 */
package io.pkts.packet.diameter;

import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;

/**
 * @author jonas@jonasborjesson.com
 */
public class DiameterTestBase {

    /**
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    public static Buffer loadBuffer(final String resource) throws Exception {
        final Path path = Paths.get(DiameterTestBase.class.getResource(resource).toURI());
        final File file = path.toFile();
        final byte[] buffer = new byte[(int)file.length()];
        final InputStream ios = new FileInputStream(path.toFile());
        final int totalRead = ios.read(buffer);
        return Buffers.wrap(buffer);
    }

    public static DiameterMessage loadDiameterMessage(final String resource) throws Exception {
        final Buffer buffer = loadBuffer(resource);
        return DiameterMessage.frame(buffer);
    }

}
