package io.pkts.diameter.codegen.primitives;

import io.pkts.diameter.codegen.CodeGenParseException;
import io.pkts.diameter.codegen.DiameterContext;
import io.pkts.diameter.codegen.builders.AttributeContext;
import io.pkts.diameter.codegen.builders.DiameterSaxBuilder;

public interface GavpPrimitive extends DiameterPrimitive {

    /**
     * The name of the XML element.
     */
    String NAME = "gavp";

    @Override
    default String getElementName() {
        return NAME;
    }

    static Builder of(final AttributeContext ctx) throws CodeGenParseException {
        ctx.ensureElementName(NAME);
        final String name = ctx.getString("name");
        return new Builder(ctx, name);
    }

    class Builder extends DiameterSaxBuilder.BaseBuilder<GavpPrimitive> {

        private final String name;

        private Builder(final AttributeContext ctx, final String name) {
            super(ctx);
            this.name = name;
        }

        @Override
        public String getElementName() {
            return NAME;
        }


        /**
         * We do not expect that there is a child attribute to the typedefn element.
         *
         * @param child
         */
        @Override
        public void attachChildBuilder(final DiameterSaxBuilder child) {
            throwException("Unexpected child element");
        }

        @Override
        public GavpPrimitive build(final DiameterContext ctx) {
            return null;
        }
    }
}
