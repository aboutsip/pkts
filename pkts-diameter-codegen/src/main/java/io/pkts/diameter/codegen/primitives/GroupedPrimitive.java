package io.pkts.diameter.codegen.primitives;

import io.pkts.diameter.codegen.CodeGenParseException;
import io.pkts.diameter.codegen.DiameterContext;
import io.pkts.diameter.codegen.builders.AttributeContext;
import io.pkts.diameter.codegen.builders.DiameterSaxBuilder;

import java.util.ArrayList;
import java.util.List;

public interface GroupedPrimitive extends DiameterPrimitive {

    /**
     * The name of the XML element.
     */
    String NAME = "grouped";

    @Override
    default String getElementName() {
        return NAME;
    }

    static Builder of(final AttributeContext ctx) throws CodeGenParseException {
        ctx.ensureElementName(NAME);
        return new Builder(ctx);
    }

    class Builder extends DiameterSaxBuilder.BaseBuilder<GroupedPrimitive> {

        /**
         * Those elements that we have builders for and that we should accept.
         */
        private static final List<String> acceptableChildElements = new ArrayList<>();

        static {
            acceptableChildElements.add(GavpPrimitive.NAME);
        }

        private Builder(final AttributeContext ctx) {
            super(ctx);
        }

        @Override
        public String getElementName() {
            return NAME;
        }

        @Override
        public GroupedPrimitive build(final DiameterContext ctx) {
            return null;
        }
    }
}
