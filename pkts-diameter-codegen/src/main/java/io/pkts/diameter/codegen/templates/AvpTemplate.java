package io.pkts.diameter.codegen.templates;

import io.pkts.diameter.codegen.config.CodeConfig;
import io.pkts.diameter.codegen.primitives.AvpPrimitive;
import liqp.RenderSettings;
import liqp.Template;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Helper class for finding and loading the appropriate template for an AVP.
 */
public class AvpTemplate {

    private final Template template;
    private final AvpPrimitive avp;

    public static AvpTemplate load(final AvpPrimitive avp) throws URISyntaxException, IOException {
        final Path p = Paths.get(AvpTemplate.class.getResource("avp_template.liquid").toURI());

        final RenderSettings settings = new RenderSettings.Builder().withStrictVariables(false).build();
        final Template template = Template.parse(p.toFile()).withRenderSettings(settings);

        return new AvpTemplate(avp, template);
    }

    private AvpTemplate(final AvpPrimitive avp, final Template template) {
        this.avp = avp;
        this.template = template;
    }

    public String render(final CodeConfig baseConfig) {
        return render(baseConfig.createAvpConfig(avp).getAttributes());
    }

    public String render(final Map<String, Object> attributes) {
        return template.render(attributes);
    }


}
