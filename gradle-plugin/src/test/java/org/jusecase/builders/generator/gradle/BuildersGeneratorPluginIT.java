package org.jusecase.builders.generator.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE;

/**
 * Builds a sample project with the plugin under test: generates the builders, compiles the project's tests against
 * them and runs those tests. Needs access to Maven Central for the sample project's dependencies.
 */
class BuildersGeneratorPluginIT {

    @TempDir
    Path projectDir;

    @Test
    void generatesBuildersAndRunsTestsAgainstThem() throws IOException {
        copySampleProject("generate-builders");

        BuildResult result = runner("test").build();

        assertThat(result.task(":generateBuilders").getOutcome()).isEqualTo(SUCCESS);
        assertThat(result.task(":test").getOutcome()).isEqualTo(SUCCESS);
        Path generated = projectDir.resolve("build/generated/sources/jusecase-builders/test/example");
        assertThat(generated.resolve("UserBuilderMethods.java")).exists();
        assertThat(generated.resolve("HiddenBuilderMethods.java")).doesNotExist();
    }

    @Test
    void generationIsUpToDateOnSecondRun() throws IOException {
        copySampleProject("generate-builders");
        runner("generateBuilders").build();

        BuildResult result = runner("generateBuilders").build();

        assertThat(result.task(":generateBuilders").getOutcome()).isEqualTo(UP_TO_DATE);
    }

    @Test
    void worksWithConfigurationCache() throws IOException {
        copySampleProject("generate-builders");
        runner("test", "--configuration-cache").build();

        BuildResult result = runner("test", "--configuration-cache").build();

        assertThat(result.getOutput()).contains("Reusing configuration cache");
    }

    private GradleRunner runner(String... arguments) {
        String[] withStacktrace = Stream.concat(Stream.of(arguments), Stream.of("--stacktrace")).toArray(String[]::new);
        return GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withPluginClasspath()
                .withArguments(withStacktrace);
    }

    private void copySampleProject(String name) throws IOException {
        Path source = Paths.get("src/test/resources/projects", name);
        try (Stream<Path> files = Files.walk(source)) {
            for (Path file : (Iterable<Path>) files::iterator) {
                Path target = projectDir.resolve(source.relativize(file).toString());
                if (Files.isDirectory(file)) {
                    Files.createDirectories(target);
                } else {
                    Files.copy(file, target);
                }
            }
        }
    }
}
