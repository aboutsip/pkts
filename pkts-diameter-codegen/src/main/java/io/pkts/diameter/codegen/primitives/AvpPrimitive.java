package io.pkts.diameter.codegen.primitives;

import io.pkts.diameter.codegen.CodeGenParseException;
import io.pkts.diameter.codegen.DiameterContext;
import io.pkts.diameter.codegen.builders.AttributeContext;
import io.pkts.diameter.codegen.builders.DiameterSaxBuilder;

import java.util.ArrayList;
import java.util.List;

public interface AvpPrimitive extends DiameterPrimitive {

    /**
     * The name of the XML element.
     */
    String NAME = "avp";

    @Override
    default String getElementName() {
        return NAME;
    }

    static Builder of(final AttributeContext ctx) throws CodeGenParseException {
        ctx.ensureElementName(NAME);

        final String name = ctx.getString("name");
        final long code = ctx.getLong("code");

        return new Builder(ctx, name, code);
    }

    class Builder extends DiameterSaxBuilder.BaseBuilder<AvpPrimitive> {

        private final String name;
        private final long code;

        /**
         * Those elements that we have builders for and that we should accept.
         */
        private static final List<String> acceptableChildElements = new ArrayList<>();

        static {
            acceptableChildElements.add(TypePrimitive.NAME);
            acceptableChildElements.add(EnumPrimitive.NAME);
            acceptableChildElements.add(GroupedPrimitive.NAME);
        }

        private Builder(final AttributeContext ctx, final String name, final long code) {
            super(ctx);
            this.name = name;
            this.code = code;
        }

        @Override
        protected List<String> getKnownChildElements() {
            return acceptableChildElements;
        }

        @Override
        public String getElementName() {
            return NAME;
        }


        @Override
        public AvpPrimitive build(final DiameterContext ctx) {
            return null;
        }

    }
}
