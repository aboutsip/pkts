package io.pkts.diameter.codegen.config;

import io.pkts.diameter.avp.Avp;
import io.pkts.diameter.avp.type.DiameterType;
import io.pkts.diameter.codegen.Typedef;
import io.pkts.diameter.codegen.primitives.AvpPrimitive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains configuration for where to render the code, the default package, how to
 * convert names from the dictionary files into Java class names and more.
 */
public class CodeConfig {

    private static final String DEFAULT_BASE_PACKAGE = "io.pkts.diameter";
    private static final String DEFAULT_AVP_PACKAGE = DEFAULT_BASE_PACKAGE + ".avp";


    private final ClassNameConverter classNameConverter = ClassNameConverter.defaultConverter();

    /**
     * All AVPs should be part of this package.
     *
     * @return
     */
    public String getAvpPackageName() {
        return DEFAULT_AVP_PACKAGE;
    }

    /**
     * @param avp
     * @return a new {@link AvpCodeConfig} instance that can be used to render the liquid template.
     */
    public Map<String, Object> createAvpConfig(final AvpPrimitive avp) {

        // annoying!
        final Map<String, Object> attributes = new HashMap<>();
        final Map<String, Object> javaAttributes = new HashMap<>();
        final Map<String, Object> javaClassAttributes = new HashMap<>();
        final Map<String, Object> avpAttributes = new HashMap<>();
        final Map<String, Object> avpTypeAttributes = new HashMap<>();

        // Java imports. Just put them here.
        final List<String> imports = new ArrayList<>();

        // build up the hierarchy of attributes.
        attributes.put("avp", avpAttributes);
        attributes.put("java", javaAttributes);
        javaAttributes.put("imports", imports);
        javaAttributes.put("class", javaClassAttributes);

        avpAttributes.put("type", avpTypeAttributes);

        // The Java interface name of our avp and the package
        final String className = classNameConverter.convert(avp);
        javaClassAttributes.put("name", className);
        javaAttributes.put("package", getAvpPackageName());

        avpAttributes.put("code", avp.getCode());

        final Typedef typedef = avp.toTyped().getTypedef();
        final Class<? extends DiameterType> typeInterface =
                typedef.getImplementingInterface().orElseThrow(() -> new IllegalArgumentException("Unable to render AVP " + avp.getName()
                        + " because missing interface definition for the type " + typedef.getName()));
       
        final Class<? extends Avp> typeClass =
                typedef.getImplementingClass().orElseThrow(IllegalArgumentException::new);

        avpTypeAttributes.put("class", typeClass.getSimpleName());
        avpTypeAttributes.put("interface", typeInterface.getSimpleName());

        imports.add(typeClass.getName());
        imports.add(typeInterface.getName());

        return attributes;
    }


}
