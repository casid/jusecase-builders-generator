package org.jusecase.builders.generator.gradle;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.jusecase.builders.generator.BuildersGenerator;

import java.net.URLClassLoader;

public abstract class GenerateBuildersWorker implements WorkAction<GenerateBuildersWorker.Parameters> {

    private static final Logger LOGGER = Logging.getLogger(GenerateBuildersWorker.class);

    public interface Parameters extends WorkParameters {
        ConfigurableFileCollection getProjectClasspath();
        DirectoryProperty getTargetDirectory();
        ListProperty<String> getPackages();
        ListProperty<String> getClasses();
        ListProperty<String> getExcludeClasses();
        ListProperty<String> getBaseClasses();
        Property<Boolean> getNestedClasses();
        Property<String> getLineSeparator();
    }

    @Override
    public void execute() {
        Parameters p = getParameters();

        try (URLClassLoader classLoader = BuildersGenerator.createClassLoader(p.getProjectClasspath().getFiles())) {
            new BuildersGenerator(
                    classLoader,
                    p.getTargetDirectory().get().getAsFile(),
                    toArray(p.getPackages()),
                    toArray(p.getClasses()),
                    toArray(p.getExcludeClasses()),
                    toArray(p.getBaseClasses()),
                    p.getNestedClasses().get(),
                    p.getLineSeparator().get(),
                    LOGGER::warn).generate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate builders: " + e.getMessage(), e);
        }
    }

    private static String[] toArray(ListProperty<String> property) {
        return property.get().toArray(new String[0]);
    }
}
