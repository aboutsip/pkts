package io.pkts.diameter.codegen;

import io.pkts.diameter.codegen.builders.AttributeContext;
import io.pkts.diameter.codegen.builders.DiameterSaxBuilder;
import io.pkts.diameter.codegen.primitives.DiameterPrimitive;

public class DiameterRootBuilder extends DiameterSaxBuilder.BaseBuilder<DiameterPrimitive> {

    public DiameterRootBuilder(final AttributeContext ctx) {
        super(ctx);
    }

    @Override
    public String getElementName() {
        return "root";
    }

    @Override
    public DiameterPrimitive build(final DiameterContext ctx) {
        return null;
    }
}

