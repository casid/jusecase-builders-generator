package org.jusecase.builders.generator.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;

@CacheableTask
public abstract class GenerateBuildersTask extends DefaultTask {

    private static final int MIN_JAVA_VERSION = 17;

    /**
     * Compiled classes and dependencies the builders are generated from.
     */
    @Classpath
    public abstract ConfigurableFileCollection getClasspath();

    /**
     * Java launcher the generator runs with. Must run Java 17 or newer.
     */
    @Nested
    public abstract Property<JavaLauncher> getJavaLauncher();

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

    /**
     * The literal line separator to generate with.
     */
    @Input
    public abstract Property<String> getLineSeparator();

    @Inject
    protected abstract WorkerExecutor getWorkerExecutor();

    @TaskAction
    public void generate() {
        // Separate process, so that the project's classes can be loaded even if they target a newer Java version
        // than the one Gradle runs on, and the generator's dependencies (e.g. Guava) cannot clash with Gradle's.
        JavaLauncher launcher = getJavaLauncher().get();
        int javaVersion = launcher.getMetadata().getLanguageVersion().asInt();
        if (javaVersion < MIN_JAVA_VERSION) {
            throw new GradleException("The builders generator requires Java " + MIN_JAVA_VERSION + " or newer, but the configured "
                    + "Java launcher runs Java " + javaVersion + ". Configure a newer toolchain or set 'javaLauncher' of generateBuilders.");
        }
        String javaExecutable = launcher.getExecutablePath().getAsFile().getAbsolutePath();
        getWorkerExecutor().processIsolation(spec -> spec.getForkOptions().setExecutable(javaExecutable)).submit(GenerateBuildersWorker.class, p -> {
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
