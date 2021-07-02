package org.jusecase.builders.generator;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.jusecase.builders.generator.entities.Card;
import org.jusecase.builders.generator.entities.Goblin;
import org.jusecase.builders.generator.entities.User;
import org.jusecase.builders.generator.entities.Vector;
import org.jusecase.builders.generator.oddities.*;
import org.jusecase.builders.generator.usecases.DummyUsecase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jusecase.Builders.an;
import static org.jusecase.Builders.inputStream;

public class BuilderGeneratorTest {
    private static final String encoding = "UTF-8";

    private Class<?> entityClass;
    private String lineSeparator = "\n";

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
    public void entityWithFinalFields() {
        givenEntityClass(EntityWithFinalFields.class);
        whenBuilderIsGenerated();
        thenGeneratedBuilderIsEqualTo("EntityWithFinalFields.expected.txt");
    }

    @Test
    public void entityWithPrimitiveArrays() {
        givenEntityClass(EntityWithPrimitiveArrays.class);
        whenBuilderIsGenerated();
        thenGeneratedBuilderIsEqualTo("EntityWithPrimitiveArrays.expected.txt");
    }

    @Test
    public void entityWithTypedArray() {
        givenEntityClass(EntityWithTypedArray.class);
        whenBuilderIsGenerated();
        thenGeneratedBuilderIsEqualTo("EntityWithTypedArray.expected.txt");
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
            assertThat(generatedOutput).isEqualTo(IOUtils.toString(an(inputStream().withResource(fileName)), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}