/**
 * 
 */
package io.pkts.framer;

import io.pkts.PktsTestBase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jonas@jonasborjesson.com
 */
public class EthernetFramerTest extends PktsTestBase {

    private EthernetFramer framer;

    /**
     * @throws java.lang.Exception
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.framer = new EthernetFramer();
    }

    /**
     * @throws java.lang.Exception
     */
    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEthernetFramerNoParent() throws Exception {
        this.framer.frame(null, this.ethernetFrameBuffer);
    }

}
