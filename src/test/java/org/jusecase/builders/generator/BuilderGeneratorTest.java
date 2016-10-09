package org.jusecase.builders.generator;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.jusecase.builders.generator.entities.User;
import org.jusecase.builders.generator.entities.Vector;
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

    private void givenEntityClass(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    private void whenBuilderIsGenerated() {
        try {

            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                BuilderGenerator generator = new BuilderGenerator(entityClass, os, encoding, lineSeparator);
                generator.generate();
                generatedOutput = os.toString(encoding);
            }
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