package org.jusecase.builders.generator.gradle;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.jusecase.builders.generator.LineSeparator;

public interface BuildersGeneratorExtension {
    /**
     * Destination dir to store generated builder interfaces.
     * Defaults to 'build/generated/sources/jusecase-builders/test'.
     */
    DirectoryProperty getTargetDirectory();

    /**
     * List of packages to generate builders for.
     */
    ListProperty<String> getPackages();

    /**
     * List of classes to generate builders for.
     */
    ListProperty<String> getClasses();

    /**
     * List of classes no builders should be generated for.
     */
    ListProperty<String> getExcludeClasses();

    /**
     * If present, only builders for classes in specified packages that inherit one of these base classes are generated.
     */
    ListProperty<String> getBaseClasses();

    /**
     * If builders for nested classes should be generated. Defaults to true.
     */
    Property<Boolean> getNestedClasses();

    /**
     * Line separator for generated builders. If not set, system default line separator is used.
     */
    Property<LineSeparator> getLineSeparator();
}
