package org.jusecase.builders.generator;

import org.junit.jupiter.api.Test;
import org.jusecase.builders.generator.entities.Card;
import org.jusecase.builders.generator.entities.Goblin;
import org.jusecase.builders.generator.entities.User;
import org.jusecase.builders.generator.entities.Vector;
import org.jusecase.builders.generator.oddities.*;
import org.jusecase.builders.generator.oddities.shadow.EntityWithShadowedJavaLangType;
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
    public void entityWithNestedJavaLangType() {
        givenEntityClass(EntityWithNestedJavaLangType.class);
        whenBuilderIsGenerated();
        thenGeneratedBuilderIsEqualTo("EntityWithNestedJavaLangType.expected.txt");
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
    public void entityWithGenericParentClass() {
        givenEntityClass(EntityWithGenericParentClass.class);
        whenBuilderIsGenerated();
        thenGeneratedBuilderIsEqualTo("EntityWithGenericParentClass.expected.txt");
    }

    @Test
    public void genericEntity() {
        givenEntityClass(GenericEntity.class);
        whenBuilderIsGenerated();
        thenGeneratedBuilderIsEqualTo("GenericEntity.expected.txt");
    }

    @Test
    public void entityOverridesParentClassMethod() {
        givenEntityClass(EntityOverridesParentClassMethod.class);
        whenBuilderIsGenerated();
        thenGeneratedBuilderIsEqualTo("EntityOverridesParentClassMethod.expected.txt");
    }

    @Test
    public void entityOverridesGenericParentClassMethod() {
        givenEntityClass(EntityOverridesGenericParentClassMethod.class);
        whenBuilderIsGenerated();
        thenGeneratedBuilderIsEqualTo("EntityOverridesGenericParentClassMethod.expected.txt");
    }

    @Test
    public void entityWithRawOverride() {
        givenEntityClass(EntityWithRawOverride.class);
        whenBuilderIsGenerated();
        thenGeneratedBuilderIsEqualTo("EntityWithRawOverride.expected.txt");
    }

    @Test
    public void entityWithFieldAndSetter() {
        givenEntityClass(EntityWithFieldAndSetter.class);
        whenBuilderIsGenerated();
        thenGeneratedBuilderIsEqualTo("EntityWithFieldAndSetter.expected.txt");
    }

    @Test
    public void genericEntityWithTypeArguments() {
        givenEntityClass(GenericEntityWithTypeArguments.class);
        whenBuilderIsGenerated();
        thenGeneratedBuilderIsEqualTo("GenericEntityWithTypeArguments.expected.txt");
    }

    @Test
    public void entityWithOwnerTypes() {
        givenEntityClass(EntityWithOwnerTypes.class);
        whenBuilderIsGenerated();
        thenGeneratedBuilderIsEqualTo("EntityWithOwnerTypes.expected.txt");
    }

    @Test
    public void entityWithShadowedJavaLangType() {
        givenEntityClass(EntityWithShadowedJavaLangType.class);
        whenBuilderIsGenerated();
        thenGeneratedBuilderIsEqualTo("EntityWithShadowedJavaLangType.expected.txt");
    }

    @Test
    public void entityWithRawParent() {
        givenEntityClass(EntityWithRawParent.class);
        whenBuilderIsGenerated();
        thenGeneratedBuilderIsEqualTo("EntityWithRawParent.expected.txt");
    }

    @Test
    public void genericEntityWithBuilderTypeNames() {
        givenEntityClass(GenericEntityWithBuilderTypeNames.class);
        whenBuilderIsGenerated();
        thenGeneratedBuilderIsEqualTo("GenericEntityWithBuilderTypeNames.expected.txt");
    }

    @Test
    public void entityWithGenericSetters() {
        givenEntityClass(EntityWithGenericSetters.class);
        whenBuilderIsGenerated();
        thenGeneratedBuilderIsEqualTo("EntityWithGenericSetters.expected.txt");
    }

    @Test
    public void genericEntityWithClashingSetterVariable() {
        givenEntityClass(GenericEntityWithClashingSetterVariable.class);
        whenBuilderIsGenerated();
        thenGeneratedBuilderIsEqualTo("GenericEntityWithClashingSetterVariable.expected.txt");
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

   @Test
   public void entityWithTypedArrayList() {
      givenEntityClass(EntityWithTypedArrayList.class);
      whenBuilderIsGenerated();
      thenGeneratedBuilderIsEqualTo("EntityWithTypedArrayList.expected.txt");
   }

   @Test
   public void entityWithTypedList() {
      givenEntityClass(EntityWithTypedList.class);
      whenBuilderIsGenerated();
      thenGeneratedBuilderIsEqualTo("EntityWithTypedList.expected.txt");
   }

   @Test
   public void entityWithNestedArray() {
        givenEntityClass(EntityWithNestedArray.class);
        whenBuilderIsGenerated();
        thenGeneratedBuilderIsEqualTo("EntityWithNestedArray.expected.txt");
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
            assertThat(generatedOutput).isEqualTo(new String(an(inputStream().withResource(fileName)).readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}