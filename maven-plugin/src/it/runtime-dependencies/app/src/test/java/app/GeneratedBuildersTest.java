package app;

import libb.Thing;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

class GeneratedBuildersTest {
    @Test
    void builderHasMethodsOfParentClassWithRuntimeOnlyTypes() {
        Thing thing = new Thing();

        Entity entity = new EntityBuilder().withThing(thing).build();

        assertSame(thing, entity.getThing());
    }
}
