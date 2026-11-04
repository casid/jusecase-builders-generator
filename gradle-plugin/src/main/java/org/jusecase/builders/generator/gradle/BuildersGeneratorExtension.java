package org.jusecase.builders.generator.gradle;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.jusecase.builders.generator.LineSeparator;

public interface BuildersGeneratorExtension {
    /**
     * Whether the target directory is added to the test sources, which makes the test compilation depend on
     * {@code generateBuilders}. Defaults to true. Set to false if the builders are checked in and generated manually.
     */
    Property<Boolean> getAddToTestSources();

    /**
     * Classes and dependencies the builders are generated from. Defaults to the production classes (main source set)
     * and their compile classpath, including compileOnly dependencies. Test classes are not part of it, since the test output
     * depends on the generated builders.
     */
    ConfigurableFileCollection getClasspath();

    /**
     * Destination dir to store generated builder interfaces.
     * Defaults to 'build/generated/sources/jusecase-builders/test'.
     * The directory must only contain generated builders: builders of earlier runs are deleted before generating.
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
     * Groovy build scripts can assign the constant name as a string (e.g. {@code lineSeparator = 'Unix'}).
     */
    Property<LineSeparator> getLineSeparator();
}
