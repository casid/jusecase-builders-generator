package org.jusecase.builders.generator;

import org.junit.jupiter.api.Test;
import org.jusecase.builders.generator.entities.Card;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URLClassLoader;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ProjectClassLoaderTest {

    @Test
    public void loadsClassesOfGivenClasspath() throws Exception {
        try (URLClassLoader classLoader = BuildersGenerator.createClassLoader(Collections.singletonList(testClassesDirectory()))) {
            Class<?> loaded = classLoader.loadClass(Card.class.getName());

            assertThat(loaded.getName()).isEqualTo(Card.class.getName());
            assertThat(loaded).isNotSameAs(Card.class);
        }
    }

    @Test
    public void doesNotSeeClassesOfTheGeneratorClasspath() throws Exception {
        ClassLoader classLoader = BuildersGenerator.createClassLoader(Collections.singletonList(testClassesDirectory()));

        assertThatThrownBy(() -> classLoader.loadClass(BuildersGenerator.class.getName()))
                .isInstanceOf(ClassNotFoundException.class);
    }

    private File testClassesDirectory() throws URISyntaxException {
        return new File(Card.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    }
}
