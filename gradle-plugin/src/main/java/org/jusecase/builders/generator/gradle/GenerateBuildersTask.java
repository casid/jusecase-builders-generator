package org.jusecase.builders.generator.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.workers.WorkerExecutor;
import org.jusecase.builders.generator.LineSeparator;

import javax.inject.Inject;

@CacheableTask
public abstract class GenerateBuildersTask extends DefaultTask {

    /**
     * Compiled classes and dependencies the builders are generated from.
     */
    @Classpath
    public abstract ConfigurableFileCollection getClasspath();

    @OutputDirectory
    public abstract DirectoryProperty getTargetDirectory();

    @Input
    public abstract ListProperty<String> getPackages();

    @Input
    public abstract ListProperty<String> getClasses();

    @Input
    public abstract ListProperty<String> getExcludeClasses();

    @Input
    public abstract ListProperty<String> getBaseClasses();

    @Input
    public abstract Property<Boolean> getNestedClasses();

    @Input
    public abstract Property<LineSeparator> getLineSeparator();

    @Inject
    protected abstract WorkerExecutor getWorkerExecutor();

    @TaskAction
    public void generate() {
        // Isolated, so that the generator's dependencies (e.g. Guava) cannot clash with Gradle's or the project's.
        getWorkerExecutor().classLoaderIsolation().submit(GenerateBuildersWorker.class, p -> {
            p.getProjectClasspath().from(getClasspath());
            p.getTargetDirectory().set(getTargetDirectory());
            p.getPackages().set(getPackages());
            p.getClasses().set(getClasses());
            p.getExcludeClasses().set(getExcludeClasses());
            p.getBaseClasses().set(getBaseClasses());
            p.getNestedClasses().set(getNestedClasses());
            p.getLineSeparator().set(getLineSeparator());
        });
    }
}
