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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import static org.apache.maven.plugins.annotations.LifecyclePhase.GENERATE_TEST_SOURCES;
import static org.apache.maven.plugins.annotations.ResolutionScope.COMPILE;

@Mojo(name = "generate-builders", defaultPhase = GENERATE_TEST_SOURCES, requiresDependencyResolution = COMPILE)
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
    public String lineSeparator = LineSeparator.Unix.toString();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try (URLClassLoader classLoader = createClassLoader()) {
            getLog().info("Generating builder interfaces...");

            warnAboutUnknownExcludes(classLoader);

            BuildersGenerator generator = new BuildersGenerator(
                    classLoader,
                    targetDirectory,
                    packages,
                    classes,
                    excludeClasses,
                    baseClasses,
                    nestedClasses,
                    LineSeparator.fromString(lineSeparator).value);
            generator.generate();
            project.addTestCompileSourceRoot(targetDirectory.getPath());

            getLog().info("Builder interfaces generated at " + targetDirectory);

        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void warnAboutUnknownExcludes(ClassLoader classLoader) {
        if (excludeClasses == null) {
            return;
        }
        for (String className : excludeClasses) {
            if (classLoader.getResource(className.replace('.', '/') + ".class") == null) {
                getLog().warn("excludeClasses entry '" + className + "' was not found on the compile classpath and has no effect. "
                        + "Test classes are not part of it.");
            }
        }
    }

    /**
     * Scans the production classes and their compile classpath. Test classes are not part of it, since they may depend
     * on the generated builders. Parent is the platform loader, not the plugin's loader: the project's own Guava,
     * builders etc. must win over the generator's copies, and scanning must not pick up the plugin's classpath.
     */
    private URLClassLoader createClassLoader() throws DependencyResolutionRequiredException, MalformedURLException {
        List<URL> urls = new ArrayList<>();
        for (String classpathElement : project.getCompileClasspathElements()) {
            urls.add(new File(classpathElement).toURI().toURL());
        }
        return new URLClassLoader(urls.toArray(new URL[0]), ClassLoader.getPlatformClassLoader());
    }
}
