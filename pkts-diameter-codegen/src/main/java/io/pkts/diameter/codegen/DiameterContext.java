package io.pkts.diameter.codegen;

import io.pkts.diameter.codegen.primitives.ApplicationPrimitive;
import io.pkts.diameter.codegen.primitives.AvpPrimitive;
import io.pkts.diameter.codegen.primitives.DiameterPrimitive;
import io.pkts.diameter.codegen.primitives.TypedefPrimitive;

import java.util.ArrayList;
import java.util.List;

/**
 * As we are parsing and eventually building up the structure as defined in the
 * various dictionary.xml files, we will at some point have to collect them all.
 * This is done while we build all the generated builders and the top-level
 * elements are collected into this {@link DiameterContext}, which then
 * eventually will contain all the high-level {@link DiameterPrimitive}.
 */
public class DiameterContext {

    private final List<AvpPrimitive> avps = new ArrayList<>();
    private final List<ApplicationPrimitive> apps = new ArrayList<>();
    private final List<TypedefPrimitive> typedefs = new ArrayList<>();

    public void collectAvp(final AvpPrimitive primitive) {
        avps.add(primitive);
    }

    public void collectApplication(final ApplicationPrimitive primitive) {
        apps.add(primitive);
    }

    public void collectTypeDef(final TypedefPrimitive primitive) {
        typedefs.add(primitive);
    }


    public List<AvpPrimitive> getAvps() {
        return avps;
    }

}
