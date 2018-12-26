package io.pkts.diameter.codegen.config;

import java.util.Map;

/**
 * When we generate the liquid template we have to supply a hierarchy of attributes for the
 * liquid template engine to work off of. Those are just maps of maps but there are other
 * attributes that we'd like easy access to, such as the name of the resulting java file name
 * and the package of it. So, this one just wraps it all.
 */
public class Attributes {

    private final Map<String, Object> attributes;
    private final String name;
    private final String packageName;

    public Attributes(final String name, final String packageName, final Map<String, Object> attributes) {
        this.name = name;
        this.packageName = packageName;
        this.attributes = attributes;
    }

    public String getName() {
        return name;
    }

    public String getPackage() {
        return packageName;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
