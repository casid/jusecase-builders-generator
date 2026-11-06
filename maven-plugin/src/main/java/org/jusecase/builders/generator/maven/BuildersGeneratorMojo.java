package org.jusecase.builders.generator.maven;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jusecase.builders.generator.BuildersGenerator;
import org.jusecase.builders.generator.LineSeparator;

import java.io.File;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.apache.maven.plugins.annotations.LifecyclePhase.GENERATE_TEST_SOURCES;
import static org.apache.maven.plugins.annotations.ResolutionScope.COMPILE_PLUS_RUNTIME;

@Mojo(name = "generate-builders", defaultPhase = GENERATE_TEST_SOURCES, requiresDependencyResolution = COMPILE_PLUS_RUNTIME)
public class BuildersGeneratorMojo extends AbstractMojo {

    @Parameter(property = "project", required = true, readonly = true)
    public MavenProject project;

    /**
     * Destination dir to store generated builder interfaces. <br>
     * Defaults to 'target/generated-test-sources/jusecase-builders'.<br>
     * Relative paths are resolved against the module. The directory is added to the test sources of the project.<br>
     * The directory must only contain generated builders: builders of earlier runs are deleted before generating.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-test-sources/jusecase-builders", property = "builders.targetDirectory")
    public File targetDirectory;

    /**
     * List of packages to generate builders for.
     */
    @Parameter(property = "builders.packages")
    public String[] packages;

    /**
     * List of classes to generate builders for.
     */
    @Parameter(property = "builders.classes")
    public String[] classes;

    /**
     * List of classes no builders should be generated for.
     */
    @Parameter(property = "builders.excludeClasses")
    public String[] excludeClasses;

    /**
     * If present, only builders for classes in specified packages that inherit one of these base classes are generated.
     */
    @Parameter(property = "builders.baseClasses")
    public String[] baseClasses;

    /**
     * If builders for nested classes should be generated.
     */
    @Parameter(property = "builders.nestedClasses")
    public boolean nestedClasses = true;

    /**
     * Line separator for generated builders. If not set, Unix line separators are used, regardless of the system.
     * @see LineSeparator
     */
    @Parameter(property = "builders.lineSeparator")
    public String lineSeparator;

    /**
     * Scans the production classes and all their dependencies, including runtime-only ones: reflection on a class fails
     * if a type it refers to is missing. Test classes are not part of it, since they may depend on the generated
     * builders.
     */
    private List<File> classpath() throws DependencyResolutionRequiredException {
        Set<String> elements = new LinkedHashSet<>(project.getCompileClasspathElements());
        elements.addAll(project.getRuntimeClasspathElements());

        List<File> files = new ArrayList<>();
        for (String element : elements) {
            files.add(new File(element));
        }
        return files;
    }

    /**
     * The goal scans compiled classes. Running it manually on a module that was not compiled would find nothing, and
     * the builders of earlier runs would be deleted for nothing.
     */
    private void checkProjectIsCompiled() throws MojoFailureException {
        File outputDirectory = new File(project.getBuild().getOutputDirectory());
        if (outputDirectory.exists()) {
            return;
        }
        for (String sourceRoot : project.getCompileSourceRoots()) {
            if (new File(sourceRoot).isDirectory()) {
                throw new MojoFailureException("The production classes of " + project.getArtifactId() + " are not compiled yet ("
                        + outputDirectory + " does not exist). Compile the module first, e.g. 'mvn compile builders-generator:generate-builders'.");
            }
        }
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        checkProjectIsCompiled();

        try (URLClassLoader classLoader = BuildersGenerator.createClassLoader(classpath())) {
            getLog().info("Generating builder interfaces...");

            BuildersGenerator generator = new BuildersGenerator(
                    classLoader,
                    targetDirectory,
                    packages,
                    classes,
                    excludeClasses,
                    baseClasses,
                    nestedClasses,
                    LineSeparator.fromString(lineSeparator).value,
                    getLog()::warn);
            generator.generate();
            project.addTestCompileSourceRoot(targetDirectory.getPath());

            getLog().info("Builder interfaces generated at " + targetDirectory);

        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}
