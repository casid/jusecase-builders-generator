package org.jusecase.builders.generator;

import org.jusecase.builders.generator.PropertiesResolver.Property;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Date;

public class BuilderGenerator {
    private final Class<?> entityClass;
    private final String lineSeparator;
    private final Charset charset;
    private final Collection<Property> properties;

    public BuilderGenerator(Class<?> entityClass, String encoding, String lineSeparator) {
        this.entityClass = entityClass;
        this.lineSeparator = lineSeparator;
        this.charset = Charset.forName(encoding);
        this.properties = new PropertiesResolver(entityClass).resolveProperties();
    }

    public void generate(OutputStream os) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, charset))) {
            TemplateProcessor classProcessor = new TemplateProcessor(Templates.BUILDER_CLASS, lineSeparator);
            classProcessor.setProperty("package", entityClass.getPackage().getName());
            classProcessor.setProperty("builder-class", getBuilderClassName());
            classProcessor.setProperty("entity-class", getEntityClassName());
            classProcessor.setProperty("body", generateBody(classProcessor));
            writer.write(classProcessor.process());
            writer.flush();
        }
    }

    public String getBuilderClassName() {
        if (entityClass.getEnclosingClass() == null) {
            return entityClass.getSimpleName() + "BuilderMethods";
        } else {
            return entityClass.getEnclosingClass().getSimpleName() + entityClass.getSimpleName() + "BuilderMethods";
        }
    }

    private String getEntityClassName() {
        if (entityClass.getEnclosingClass() == null) {
            return entityClass.getSimpleName();
        } else {
            return entityClass.getEnclosingClass().getSimpleName() + "." + entityClass.getSimpleName();
        }
    }

    private String generateBody(TemplateProcessor classProcessor) {
        StringBuilder body = new StringBuilder();
        addConstruction(classProcessor, body);
        addMethods(classProcessor, body);
        return body.toString();
    }

    private void addMethods(TemplateProcessor classProcessor, StringBuilder body) {
        int remainingProperties = properties.size();

        for (Property property : properties) {
            addMethod(classProcessor, body, property);

            if (--remainingProperties > 0) {
                body.append(lineSeparator);
                body.append(lineSeparator);
            }
        }
    }

    private void addMethod(TemplateProcessor classProcessor, StringBuilder body, Property property) {
        TemplateProcessor methodProcessor = getMethodTemplateProcessor(property);
        methodProcessor.setProperties(classProcessor);
        methodProcessor.setProperty("property-name", property.name);
        methodProcessor.setProperty("property-type", property.getTypeString());
        methodProcessor.setProperty("property-name-starting-with-uppercase", property.nameStartingWithUppercase());
        methodProcessor.setProperty("property-value", "value");
        body.append(methodProcessor.process());

        if (property.type.equals(Date.class)) {
            body.append(lineSeparator);
            body.append(lineSeparator);

            methodProcessor.setProperties(classProcessor);
            methodProcessor.setProperty("property-type", "String");
            methodProcessor.setProperty("property-value", "org.jusecase.Builders.a(org.jusecase.Builders.date(value))");
            body.append(methodProcessor.process());
        }
    }

    private TemplateProcessor getMethodTemplateProcessor(Property property) {
        if (property.isSetter) {
            return new TemplateProcessor(Templates.BUILDER_METHOD_SETTER, lineSeparator);
        } else {
            return new TemplateProcessor(Templates.BUILDER_METHOD_DIRECT, lineSeparator);
        }
    }

    private void addConstruction(TemplateProcessor classProcessor, StringBuilder body) {
        TemplateProcessor constructionProcessor = new TemplateProcessor(Templates.BUILDER_CONSTRUCTION, lineSeparator);
        constructionProcessor.setProperties(classProcessor);
        body.append(constructionProcessor.process());
        body.append(lineSeparator);
        body.append(lineSeparator);
    }

    public boolean isBuildable() {
        return !properties.isEmpty();
    }
}
