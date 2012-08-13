/**
 * 
 */
package com.aboutsip.yajpcap.framer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.aboutsip.yajpcap.YajTestBase;
import com.aboutsip.yajpcap.framer.layer2.EthernetFramer;

/**
 * @author jonas@jonasborjesson.com
 */
public class EthernetFramerTest extends YajTestBase {

    private EthernetFramer framer;

    /**
     * @throws java.lang.Exception
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.framer = new EthernetFramer(this.framerManager);
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
