package io.pkts.diameter.codegen.primitives;

import io.pkts.diameter.codegen.CodeGenParseException;
import io.pkts.diameter.codegen.DiameterCollector;
import io.pkts.diameter.codegen.builders.AttributeContext;
import io.pkts.diameter.codegen.builders.DiameterSaxBuilder;

public interface EnumPrimitive extends DiameterPrimitive {

    /**
     * The name of the XML element.
     */
    String NAME = "enum";

    @Override
    default String getElementName() {
        return NAME;
    }

    @Override
    default EnumPrimitive toEnumPrimitive() throws ClassCastException {
        return this;
    }

    static Builder of(final AttributeContext ctx) throws CodeGenParseException {
        ctx.ensureElementName(NAME);

        final String name = ctx.getString("name");
        final long code = ctx.getLong("code");
        return new Builder(ctx, name, code);
    }

    class Builder extends DiameterSaxBuilder.BaseBuilder<EnumPrimitive> {

        private final String name;
        private final long code;

        private Builder(final AttributeContext ctx, final String name, final long code) {
            super(ctx);
            this.name = name;
            this.code = code;
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
            throw createException("Unexpected child element");
        }

        @Override
        public EnumPrimitive build(final DiameterCollector ctx) {
            return new DefaultEnumPrimitive(name, code);
        }
    }

    class DefaultEnumPrimitive implements EnumPrimitive {

        private final String name;
        private final long code;

        private DefaultEnumPrimitive(final String name, final long code) {
            this.name = name;
            this.code = code;
        }
    }
}
