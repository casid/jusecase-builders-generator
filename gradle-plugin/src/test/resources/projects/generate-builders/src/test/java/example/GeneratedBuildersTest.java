package example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GeneratedBuildersTest {
    @Test
    void generatedBuilderMethodsAreUsable() {
        User user = new UserBuilder().withId(7).withName("Alice").build();

        assertEquals(7, user.getId());
        assertEquals("Alice", user.getName());
    }

    @Test
    void excludedClassHasNoBuilder() {
        assertThrows(ClassNotFoundException.class, () -> Class.forName("example.HiddenBuilderMethods"));
    }
}
