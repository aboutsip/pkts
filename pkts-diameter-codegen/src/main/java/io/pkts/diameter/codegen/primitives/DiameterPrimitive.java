package io.pkts.diameter.codegen.primitives;

/**
 * Base interface for our code generation for generating various diameter "primitives".
 */
public interface DiameterPrimitive {

    /**
     * The name of the primitive as it is used in the XML dictionary files.
     *
     * @return
     */
    String getElementName();
}
