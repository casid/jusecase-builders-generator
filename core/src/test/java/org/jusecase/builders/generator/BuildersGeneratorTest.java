package org.jusecase.builders.generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.jusecase.Builders.an;
import static org.jusecase.Builders.inputStream;

public class BuildersGeneratorTest {
    BuildersGenerator generator;

    File targetDirectory;
    String[] packages;
    String[] classes;
    String[] excludeClasses;
    String[] baseClasses;
    boolean nestedClasses;
    String lineSeparator;

    @TempDir
    public Path temporaryFolder;

    @BeforeEach
    public void setUp() {
        givenTargetDirectory("");
        nestedClasses = true;
        lineSeparator = "\n";
    }

    @Test
    public void noPackages() {
        givenPackages();
        whenBuildersAreGenerated();
        thenNumberOfGeneratedBuildersIs(0);
    }

    @Test
    public void packageThatDoesNotExist() {
        givenPackages("org.jusecase.builders.generator.doesnotexisthihi");
        whenBuildersAreGenerated();
        thenNumberOfGeneratedBuildersIs(0);
    }

    @Test
    public void invalidPackage() {
        givenPackages("ää$%eigjero824");
        whenBuildersAreGenerated();
        thenNumberOfGeneratedBuildersIs(0);
    }

    @Test
    public void entitiesPackage_amountOfBuildersIsCorrect() {
        givenPackages("org.jusecase.builders.generator.entities");

        whenBuildersAreGenerated();

        thenNumberOfGeneratedBuildersIs(6);
        thenBuilderIsGenerated("org/jusecase/builders/generator/entities/CardBuilderMethods.java");
        thenBuilderIsGenerated("org/jusecase/builders/generator/entities/GameEntityBuilderMethods.java");
        thenBuilderIsGenerated("org/jusecase/builders/generator/entities/GoblinBuilderMethods.java");
        thenBuilderIsGenerated("org/jusecase/builders/generator/entities/UserBuilderMethods.java");
        thenBuilderIsGenerated("org/jusecase/builders/generator/entities/VectorBuilderMethods.java");
        thenBuilderIsGenerated("org/jusecase/builders/generator/entities/EntityWithMarkerBuilderMethods.java");
    }

    @Test
    public void entitiesPackage_excludeClasses() {
        givenPackages("org.jusecase.builders.generator.entities");
        givenExcludeClasses(
              "org.jusecase.builders.generator.entities.Card",
              "org.jusecase.builders.generator.entities.Vector",
              "org.jusecase.builders.generator.entities.EntityWithMarker"
        );

        whenBuildersAreGenerated();

        thenNumberOfGeneratedBuildersIs(3);
        thenBuilderIsGenerated("org/jusecase/builders/generator/entities/GameEntityBuilderMethods.java");
        thenBuilderIsGenerated("org/jusecase/builders/generator/entities/GoblinBuilderMethods.java");
        thenBuilderIsGenerated("org/jusecase/builders/generator/entities/UserBuilderMethods.java");
    }

    @Test
    public void entitiesPackage_baseClass() {
        givenPackages("org.jusecase.builders.generator.entities");
        givenBaseClasses("org.jusecase.builders.generator.entities.EntityMarker");

        whenBuildersAreGenerated();

        thenNumberOfGeneratedBuildersIs(1);
        thenBuilderIsGenerated("org/jusecase/builders/generator/entities/EntityWithMarkerBuilderMethods.java");
    }

    @Test
    public void entitiesPackage_builderContentIsCorrect() {
        givenPackages("org.jusecase.builders.generator.entities");
        whenBuildersAreGenerated();
        thenBuilderIsGenerated("org/jusecase/builders/generator/entities/CardBuilderMethods.java", "CardBuilder.expected.txt");
    }

    @Test
    public void usecasePackage_notNested() {
        nestedClasses = false;
        givenPackages("org.jusecase.builders.generator.usecases");

        whenBuildersAreGenerated();

        thenNumberOfGeneratedBuildersIs(0);
    }

    @Test
    public void usecasePackage_nested() {
        nestedClasses = true;
        givenPackages("org.jusecase.builders.generator.usecases");

        whenBuildersAreGenerated();

        thenNumberOfGeneratedBuildersIs(2);
        thenBuilderIsGenerated("org/jusecase/builders/generator/usecases/DummyUsecaseRequestBuilderMethods.java", "DummyUsecase.Request.expected.txt");
        thenBuilderIsGenerated("org/jusecase/builders/generator/usecases/DummyUsecaseResponseBuilderMethods.java");
    }

    @Test
    public void noClasses() {
        givenClasses();
        whenBuildersAreGenerated();
        thenNumberOfGeneratedBuildersIs(0);
    }

    @Test
    public void specificClasses() {
        givenClasses(
                "org.jusecase.builders.generator.entities.Card",
                "org.jusecase.builders.generator.usecases.DummyUsecase$Request"
        );

        whenBuildersAreGenerated();

        thenNumberOfGeneratedBuildersIs(2);
    }

    @Test
    public void specificClassThatDoesNotExist_fails() {
        givenClasses("org.jusecase.builders.generator.entities.DoesNotExist");

        assertThatThrownBy(this::whenBuildersAreGenerated)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("org.jusecase.builders.generator.entities.DoesNotExist");
    }

    @Test
    public void specificNestedClasses() {
        givenClasses("org.jusecase.builders.generator.usecases.DummyUsecase");

        whenBuildersAreGenerated();

        thenNumberOfGeneratedBuildersIs(2);
        thenBuilderIsGenerated("org/jusecase/builders/generator/usecases/DummyUsecaseRequestBuilderMethods.java", "DummyUsecase.Request.expected.txt");
        thenBuilderIsGenerated("org/jusecase/builders/generator/usecases/DummyUsecaseResponseBuilderMethods.java");
    }

    @Test
    public void staleGeneratedFilesAreRemoved() throws IOException {
        Path stale = temporaryFolder.resolve("org/jusecase/old/RemovedBuilderMethods.java");
        Files.createDirectories(stale.getParent());
        Files.write(stale, "@Generated(\"jusecase-builders-generator\")".getBytes(StandardCharsets.UTF_8));
        givenClasses("org.jusecase.builders.generator.entities.Card");

        whenBuildersAreGenerated();

        assertThat(stale.getParent()).doesNotExist();
        thenNumberOfGeneratedBuildersIs(1);
    }

    @Test
    public void foreignFilesInTargetDirectory_failsWithoutDeletingAnything() throws IOException {
        Path stale = temporaryFolder.resolve("org/jusecase/old/RemovedBuilderMethods.java");
        Files.createDirectories(stale.getParent());
        Files.write(stale, "@Generated(\"jusecase-builders-generator\")".getBytes(StandardCharsets.UTF_8));
        Path handWritten = temporaryFolder.resolve("org/jusecase/keep/HandWritten.java");
        Files.createDirectories(handWritten.getParent());
        Files.write(handWritten, "class HandWritten {}".getBytes(StandardCharsets.UTF_8));
        givenClasses("org.jusecase.builders.generator.entities.Card");

        assertThatThrownBy(this::whenBuildersAreGenerated)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("HandWritten.java");

        assertThat(stale).exists();
        assertThat(handWritten).exists();
    }

    @Test
    public void hiddenFilesInTargetDirectory_areIgnoredAndKept() throws IOException {
        Path stale = temporaryFolder.resolve("org/jusecase/old/RemovedBuilderMethods.java");
        Files.createDirectories(stale.getParent());
        Files.write(stale, "@Generated(\"jusecase-builders-generator\")".getBytes(StandardCharsets.UTF_8));
        Path dsStore = temporaryFolder.resolve("org/jusecase/old/.DS_Store");
        Files.write(dsStore, new byte[]{0});
        Path gitkeep = temporaryFolder.resolve(".gitkeep");
        Files.write(gitkeep, new byte[0]);
        Path hiddenDirFile = temporaryFolder.resolve(".idea/HandWritten.java");
        Files.createDirectories(hiddenDirFile.getParent());
        Files.write(hiddenDirFile, "class HandWritten {}".getBytes(StandardCharsets.UTF_8));
        givenClasses("org.jusecase.builders.generator.entities.Card");

        whenBuildersAreGenerated();

        assertThat(stale).doesNotExist();
        assertThat(dsStore).exists();
        assertThat(gitkeep).exists();
        assertThat(hiddenDirFile).exists();
        thenBuilderIsGenerated("org/jusecase/builders/generator/entities/CardBuilderMethods.java");
    }

    @Test
    public void symlinkInTargetDirectory_failsWithoutDeletingAnything() throws IOException {
        Path stale = temporaryFolder.resolve("org/jusecase/old/RemovedBuilderMethods.java");
        Files.createDirectories(stale.getParent());
        Files.write(stale, "@Generated(\"jusecase-builders-generator\")".getBytes(StandardCharsets.UTF_8));
        Path link = temporaryFolder.resolve("org/jusecase/shared");
        Files.createSymbolicLink(link, stale.getParent());
        givenClasses("org.jusecase.builders.generator.entities.Card");

        assertThatThrownBy(this::whenBuildersAreGenerated)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("symbolic link")
                .hasMessageContaining("shared");

        assertThat(stale).exists();
        assertThat(link).exists();
    }

    @Test
    public void symlinkAsTargetDirectory_fails() throws IOException {
        Path real = Files.createDirectories(temporaryFolder.resolve("real"));
        Path link = temporaryFolder.resolve("link");
        Files.createSymbolicLink(link, real);
        givenTargetDirectory("link");
        givenClasses("org.jusecase.builders.generator.entities.Card");

        assertThatThrownBy(this::whenBuildersAreGenerated)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("is a symbolic link");
    }

    private void thenNumberOfGeneratedBuildersIs(long expected) {
        try {
            long actual = Files.walk(Paths.get(targetDirectory.getAbsolutePath())).filter(Files::isRegularFile).count();
            assertThat(actual).isEqualTo(expected);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void thenBuilderIsGenerated(String javaFileName) {
        File javaFile = new File(targetDirectory, javaFileName);
        assertThat(javaFile).exists();
    }

    private void thenBuilderIsGenerated(String javaFileName, String expectedContentFile) {
        thenBuilderIsGenerated(javaFileName);

        try {
            String actual = Files.readString(targetDirectory.toPath().resolve(javaFileName));
            String expected = new String(an(inputStream().withResource(expectedContentFile)).readAllBytes(), StandardCharsets.UTF_8);
            assertThat(actual).isEqualTo(expected);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void givenTargetDirectory(String relativePath) {
        this.targetDirectory = new File(temporaryFolder.toFile(), relativePath);
    }

    private void givenPackages(String ... packages) {
        this.packages = packages;
    }

    private void givenClasses(String ... classes) {
        this.classes = classes;
    }

    private void givenExcludeClasses(String ... classes) {
        this.excludeClasses = classes;
    }

    private void givenBaseClasses(String ... baseClasses) {
        this.baseClasses = baseClasses;
    }

    private void whenBuildersAreGenerated() {
        generator = new BuildersGenerator(Thread.currentThread().getContextClassLoader(), targetDirectory, packages, classes, excludeClasses, baseClasses, nestedClasses, lineSeparator);
        generator.generate();
    }
}