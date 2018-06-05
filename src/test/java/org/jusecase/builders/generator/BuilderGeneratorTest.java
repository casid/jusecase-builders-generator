package org.jusecase.builders.generator;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.jusecase.builders.generator.entities.*;
import org.jusecase.builders.generator.oddities.*;
import org.jusecase.builders.generator.usecases.DummyUsecase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jusecase.Builders.an;
import static org.jusecase.Builders.inputStream;

public class BuilderGeneratorTest {
    private Class<?> entityClass;
    private String lineSeparator = "\n";
    private String encoding = "UTF-8";

    private String generatedOutput;

    @Test
    public void user() {
        givenEntityClass(User.class);
        whenBuilderIsGenerated();
        thenGeneratedBuilderIsEqualTo("UserBuilder.expected.txt");
    }

    @Test
    public void user_lineSeparator() {
        givenEntityClass(User.class);
        lineSeparator = "\r\n";

        whenBuilderIsGenerated();
        thenGeneratedBuilderIsEqualTo("UserBuilder-lineSeparator.expected.txt");
    }

    @Test
    public void vector() {
        givenEntityClass(Vector.class);
        whenBuilderIsGenerated();
        thenGeneratedBuilderIsEqualTo("VectorBuilder.expected.txt");
    }

    @Test
    public void dummyUsecaseRequest() {
        givenEntityClass(DummyUsecase.Request.class);
        whenBuilderIsGenerated();
        thenGeneratedBuilderIsEqualTo("DummyUsecase.Request.expected.txt");
    }

    @Test
    public void goblin() {
        givenEntityClass(Goblin.class);
        whenBuilderIsGenerated();
        thenGeneratedBuilderIsEqualTo("GoblinBuilder.expected.txt");
    }

    @Test
    public void card() {
        givenEntityClass(Card.class);
        whenBuilderIsGenerated();
        thenGeneratedBuilderIsEqualTo("CardBuilder.expected.txt");
    }

    @Test
    public void entityWithNestedEnum() {
        givenEntityClass(EntityWithNestedEnum.class);
        whenBuilderIsGenerated();
        thenGeneratedBuilderIsEqualTo("EntityWithNestedEnum.expected.txt");
    }

    @Test
    public void entityWithOverloadedMethods() {
        givenEntityClass(EntityWithOverloadedMethods.class);
        whenBuilderIsGenerated();
        thenGeneratedBuilderIsEqualTo("EntityWithOverloadedMethods.expected.txt");
    }

    @Test
    public void entityWithParentClass() {
        givenEntityClass(EntityWithParentClass.class);
        whenBuilderIsGenerated();
        thenGeneratedBuilderIsEqualTo("EntityWithParentClass.expected.txt");
    }

    @Test
    public void entityOverridesParentClassMethod() {
        givenEntityClass(EntityOverridesParentClassMethod.class);
        whenBuilderIsGenerated();
        thenGeneratedBuilderIsEqualTo("EntityOverridesParentClassMethod.expected.txt");
    }

    @Test
    public void entityWithDates() {
        givenEntityClass(EntityWithDates.class);
        whenBuilderIsGenerated();
        thenGeneratedBuilderIsEqualTo("EntityWithDates.expected.txt");
    }

    private void givenEntityClass(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    private void whenBuilderIsGenerated() {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            BuilderGenerator generator = new BuilderGenerator(entityClass, encoding, lineSeparator);
            generator.generate(os);
            generatedOutput = os.toString(encoding);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void thenGeneratedBuilderIsEqualTo(String fileName) {
        try {
            assertThat(generatedOutput).isEqualTo(IOUtils.toString(an(inputStream().withResource(fileName))));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}