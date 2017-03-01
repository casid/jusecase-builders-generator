package org.jusecase.builders.generator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jusecase.Builders.an;
import static org.jusecase.Builders.inputStream;

public class BuildersGeneratorTest {
    BuildersGenerator generator;

    File targetDirectory;
    String[] packages;
    String[] classes;
    boolean nestedClasses;
    String lineSeparator;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();


    @Before
    public void setUp() throws Exception {
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

        thenNumberOfGeneratedBuildersIs(5);
        thenBuilderIsGenerated("org/jusecase/builders/generator/entities/CardBuilderMethods.java");
        thenBuilderIsGenerated("org/jusecase/builders/generator/entities/GameEntityBuilderMethods.java");
        thenBuilderIsGenerated("org/jusecase/builders/generator/entities/GoblinBuilderMethods.java");
        thenBuilderIsGenerated("org/jusecase/builders/generator/entities/UserBuilderMethods.java");
        thenBuilderIsGenerated("org/jusecase/builders/generator/entities/VectorBuilderMethods.java");
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
    public void specificNestedClasses() {
        givenClasses("org.jusecase.builders.generator.usecases.DummyUsecase");

        whenBuildersAreGenerated();

        thenNumberOfGeneratedBuildersIs(2);
        thenBuilderIsGenerated("org/jusecase/builders/generator/usecases/DummyUsecaseRequestBuilderMethods.java", "DummyUsecase.Request.expected.txt");
        thenBuilderIsGenerated("org/jusecase/builders/generator/usecases/DummyUsecaseResponseBuilderMethods.java");
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
            String actual = FileUtils.readFileToString(new File(targetDirectory, javaFileName));
            String expected = IOUtils.toString(an(inputStream().withResource(expectedContentFile)));
            assertThat(actual).isEqualTo(expected);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void givenTargetDirectory(String relativePath) {
        this.targetDirectory = new File(temporaryFolder.getRoot(), relativePath);
    }

    private void givenPackages(String ... packages) {
        this.packages = packages;
    }

    private void givenClasses(String ... classes) {
        this.classes = classes;
    }

    private void whenBuildersAreGenerated() {
        generator = new BuildersGenerator(Thread.currentThread().getContextClassLoader(), targetDirectory, packages, classes, nestedClasses, lineSeparator);
        generator.generate();
    }
}