package org.jusecase.builders.generator;

import org.junit.jupiter.api.Test;
import org.jusecase.builders.generator.oddities.EntityWithCheckedExceptionSetters;
import org.jusecase.builders.generator.oddities.EntityWithFieldAndSetterStartingWithI;
import org.jusecase.builders.generator.oddities.EntityWithNonAsciiNames;
import org.jusecase.builders.generator.oddities.EntityWithStaticSetter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class PropertiesResolverTest {
    private Collection<PropertiesResolver.Property> properties;
    private final List<String> warnings = new ArrayList<>();

    @Test
    public void settersWithCheckedExceptions_areSkipped() {
        whenPropertiesAreResolved(EntityWithCheckedExceptionSetters.class);
        thenPropertyNamesAre("Plain", "ThrowingUnchecked");
    }

    @Test
    public void staticSetters_areSkipped() {
        whenPropertiesAreResolved(EntityWithStaticSetter.class);
        thenPropertyNamesAre("Local");
    }

    @Test
    public void nonAsciiNames_areRejected() {
        whenPropertiesAreResolved(EntityWithNonAsciiNames.class);
        thenPropertyNamesAre("Anderung", "name");
    }

    @Test
    public void fieldAndSetterAreDeduplicated_independentOfDefaultLocale() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr", "TR"));
            whenPropertiesAreResolved(EntityWithFieldAndSetterStartingWithI.class);
        } finally {
            Locale.setDefault(original);
        }
        thenPropertyNamesAre("Id");
    }

    @Test
    public void nonAsciiNames_areWarnedAbout() {
        whenPropertiesAreResolved(EntityWithNonAsciiNames.class);

        assertThat(warnings).hasSize(4);
        assertThat(warnings).anySatisfy(w -> assertThat(w).contains("größe", EntityWithNonAsciiNames.class.getName(), "ASCII"));
        assertThat(warnings).anySatisfy(w -> assertThat(w).contains("setÜberschrift"));
        assertThat(warnings).anySatisfy(w -> assertThat(w).contains("setÄnderung"));
        assertThat(warnings).anySatisfy(w -> assertThat(w).contains("setCafé"));
    }

    @Test
    public void asciiNames_doNotWarn() {
        whenPropertiesAreResolved(EntityWithStaticSetter.class);
        assertThat(warnings).isEmpty();
    }

    private void whenPropertiesAreResolved(Class<?> entityClass) {
        properties = new PropertiesResolver(entityClass, warnings::add).resolveProperties();
    }

    private void thenPropertyNamesAre(String... names) {
        assertThat(properties.stream().map(p -> p.name).collect(Collectors.toList())).containsExactlyInAnyOrder(names);
    }
}
