package io.pkts.diameter.codegen;

import io.pkts.diameter.codegen.config.CodeConfig;
import org.junit.Before;
import org.junit.Test;

public class RenderTest extends CodeGenTestBase {

    private CodeConfig config;

    @Override
    @Before
    public void setUp() {
        super.setUp();
        config = new CodeConfig();
    }

    @Test
    public void testConvertElement() throws Exception {
        final DiameterCollector collector = load("avp001.xml");
        final Renderer renderer = new Renderer(config, collector);
        renderer.renderAvp("Origin-Host");
    }
}
