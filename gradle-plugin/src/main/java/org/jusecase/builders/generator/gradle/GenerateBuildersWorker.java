package org.jusecase.builders.generator.gradle;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.jusecase.builders.generator.BuildersGenerator;
import org.jusecase.builders.generator.LineSeparator;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public abstract class GenerateBuildersWorker implements WorkAction<GenerateBuildersWorker.Parameters> {

    public interface Parameters extends WorkParameters {
        ConfigurableFileCollection getProjectClasspath();
        DirectoryProperty getTargetDirectory();
        ListProperty<String> getPackages();
        ListProperty<String> getClasses();
        ListProperty<String> getExcludeClasses();
        ListProperty<String> getBaseClasses();
        Property<Boolean> getNestedClasses();
        Property<LineSeparator> getLineSeparator();
    }

    @Override
    public void execute() {
        Parameters p = getParameters();

        try (URLClassLoader classLoader = createClassLoader(p)) {
            new BuildersGenerator(
                    classLoader,
                    p.getTargetDirectory().get().getAsFile(),
                    toArray(p.getPackages()),
                    toArray(p.getClasses()),
                    toArray(p.getExcludeClasses()),
                    toArray(p.getBaseClasses()),
                    p.getNestedClasses().get(),
                    p.getLineSeparator().get().value).generate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate builders: " + e.getMessage(), e);
        }
    }

    private static URLClassLoader createClassLoader(Parameters p) throws Exception {
        List<URL> urls = new ArrayList<>();
        for (File file : p.getProjectClasspath()) {
            urls.add(file.toURI().toURL());
        }
        return new URLClassLoader(urls.toArray(new URL[0]), Thread.currentThread().getContextClassLoader());
    }

    private static String[] toArray(ListProperty<String> property) {
        return property.get().toArray(new String[0]);
    }
}
