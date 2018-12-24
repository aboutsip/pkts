package io.pkts.diameter.codegen.builders;

import io.pkts.diameter.codegen.CodeGenParseException;
import io.pkts.diameter.codegen.PreConditions;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import java.util.Optional;

/**
 * Helper class for the common operations on {@link Attributes}
 */
public class AttributeContext {
    private static final String ERROR_MSG = "Element doesn't contain the %s attribute";

    private final String elementName;
    private final Locator locator;
    private final Attributes attributes;

    public AttributeContext(final String elementName, final Locator locator, final Attributes attributes) {
        this.elementName = elementName;
        this.locator = locator;
        this.attributes = attributes;
    }

    public Locator getLocator() {
        return locator;
    }

    public String getElementName() {
        return elementName;
    }

    public long getLong(final String name) throws CodeGenParseException {
        final String value = getString(name);
        try {
            return Long.parseLong(value);
        } catch (final NumberFormatException e) {
            final String msg = String.format("Unable to parse attribute value '%s' as long for attribute '%s'", value, name);
            throw new CodeGenParseException(locator, msg);
        }
    }

    public int getInt(final String name) throws CodeGenParseException {
        final String value = getString(name);
        try {
            return Integer.parseInt(value);
        } catch (final NumberFormatException e) {
            final String msg = String.format("Unable to parse attribute value '%s' as int for attribute '%s'", value, name);
            throw new CodeGenParseException(locator, msg);
        }
    }

    public String getString(final String name) throws CodeGenParseException {
        final int index = attributes.getIndex(name);
        PreConditions.assertArgument(index > -1, String.format(ERROR_MSG, name), locator);
        return attributes.getValue(index);
    }

    public Optional<String> getOptionalString(final String name) throws CodeGenParseException {
        final int index = attributes.getIndex(name);
        if (index == -1) {
            return Optional.empty();
        }

        final String value = attributes.getValue(index);

        // some special cases for what empty means as defined in the wireshark xml
        if ("none".equalsIgnoreCase(value) || PreConditions.checkIfEmpty(value)) {
            return Optional.empty();
        }
        return Optional.of(value);
    }

    public void ensureElementName(final String expectedName) {
        if (!expectedName.equals(elementName)) {
            throw new CodeGenParseException(locator, String.format("Unable to handle element '%s', I " +
                    "only handle elements named '%s'", elementName, expectedName));
        }
    }
}
