package org.jusecase.builders.generator.maven;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jusecase.builders.generator.BuildersGenerator;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import static org.apache.maven.plugins.annotations.LifecyclePhase.GENERATE_TEST_SOURCES;
import static org.apache.maven.plugins.annotations.ResolutionScope.TEST;

@Mojo(name = "generate-builders", defaultPhase = GENERATE_TEST_SOURCES, requiresDependencyResolution = TEST)
public class BuildersGeneratorMojo extends AbstractMojo {

    @Parameter(property = "project", required = true, readonly = true)
    public MavenProject project;

    /**
     * Destination dir to store generated builder interfaces. <br>
     * Defaults to 'target/generated-test-sources/jusecase-builders'.<br>
     * Your IDE should be able to pick up files from this location as sources automatically when generated.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-test-sources/jusecase-builders", property = "builders.targetDirectory")
    public String targetDirectory;

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
     * If builders for nested classes should be generated.
     */
    @Parameter(property = "builders.nestedClasses")
    public boolean nestedClasses = true;

    /**
     * Line separator for generated builders. If not set, system default line separator is used.
     */
    @Parameter(property = "builders.lineSeparator")
    public String lineSeparator = System.lineSeparator();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            getLog().info("Generating builder interfaces...");

            BuildersGenerator generator = new BuildersGenerator(
                    getProjectClassLoader(),
                    new File(targetDirectory),
                    packages,
                    classes,
                    nestedClasses,
                    lineSeparator);
            generator.generate();

            getLog().info("Builder interfaces generated at " + targetDirectory);

        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private ClassLoader getProjectClassLoader() throws DependencyResolutionRequiredException, MalformedURLException {
        List<String> classpathElements = new ArrayList<>(project.getCompileClasspathElements());
        classpathElements.addAll(project.getTestClasspathElements());
        List<URL> classpathElementUrls = new ArrayList<>(classpathElements.size());
        for (String classpathElement : classpathElements) {
            classpathElementUrls.add(new File(classpathElement).toURI().toURL());
        }
        return new URLClassLoader(classpathElementUrls.toArray(new URL[0]), Thread.currentThread().getContextClassLoader());
    }
}
