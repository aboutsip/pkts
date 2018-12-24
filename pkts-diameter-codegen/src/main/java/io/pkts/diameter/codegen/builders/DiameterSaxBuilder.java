package io.pkts.diameter.codegen.builders;

import io.pkts.diameter.codegen.CodeGenParseException;
import io.pkts.diameter.codegen.DiameterContext;
import io.pkts.diameter.codegen.primitives.ApplicationPrimitive;
import io.pkts.diameter.codegen.primitives.AvpPrimitive;
import io.pkts.diameter.codegen.primitives.DiameterPrimitive;
import io.pkts.diameter.codegen.primitives.TypePrimitive;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface DiameterSaxBuilder<T extends DiameterPrimitive> {

    void characters(final char[] ch, final int start, final int length) throws SAXException;

    void attachChildBuilder(DiameterSaxBuilder<T> child) throws CodeGenParseException;

    /**
     * The name of the XML element for which this builder applies.
     *
     * @return
     */
    String getElementName();

    default boolean isAvp() {
        return AvpPrimitive.NAME.equals(getElementName());
    }

    default boolean isApplication() {
        return ApplicationPrimitive.NAME.equals(getElementName());
    }

    default boolean isType() {
        return TypePrimitive.NAME.equals(getElementName());
    }

    default boolean isUnknown() {
        return false;
    }

    T build(DiameterContext ctx);

    abstract class BaseBuilder<T extends DiameterPrimitive> implements DiameterSaxBuilder<T> {

        private final AttributeContext ctx;

        private final Map<String, List<DiameterSaxBuilder>> children = new HashMap<>();

        protected BaseBuilder(final AttributeContext ctx) {
            this.ctx = ctx;
        }

        protected List<String> getKnownChildElements() {
            // perhaps a bit odd but if there is no known list then we assume
            // accept all. Result of gradually building up the sax builders
            // while learning diameter as a protocol and the dictionary xml files.
            return null;
        }

        protected List<String> getIgnoreChildElements() {
            return null;
        }

        protected boolean throwException(final String msg) throws CodeGenParseException {
            throw new CodeGenParseException(ctx.getLocator(), msg);
        }

        /**
         * Sub-builders can decide whether or not they want to rejet the child element (builder).
         * The sub-builder can do so silently by just returning false, or loudly by throwing an
         * exception. Use exception when you do not expect a particular child-element and you want
         * the world to know so that we can either fix the dictionary.xml file or add the missing
         * code.
         *
         * @param child
         * @return
         */
        protected boolean acceptChild(final DiameterSaxBuilder child) throws CodeGenParseException {

            if (getKnownChildElements() == null || getKnownChildElements().contains(child.getElementName())) {
                return true;
            }

            if (getIgnoreChildElements() == null || getIgnoreChildElements().contains(child.getElementName())) {
                // ok, don't get confused. We recognize the element but we don't
                // want to accept it but we are also not complaining loudly about it.
                // Hence, we'll just return false.
                return false;
            }

            // ok, unknown element for which we do not have a policy (accept or silently ignore).
            // complain...
            return throwException("Un-acceptable child element '" + child.getElementName()
                    + "'. Please either fix the XML file or add missing code to accept this child element");
        }

        @Override
        public void characters(final char[] ch, final int start, final int length) throws SAXException {
            throw new CodeGenParseException(ctx.getLocator(), "Did not expect to get any additional characters");
        }

        @Override
        public void attachChildBuilder(final DiameterSaxBuilder child) throws CodeGenParseException {
            if (acceptChild(child)) {
                children.computeIfAbsent(child.getElementName(), key -> new ArrayList<>()).add(child);
            }
        }

    }

}
