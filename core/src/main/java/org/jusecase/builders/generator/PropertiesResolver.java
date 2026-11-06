package org.jusecase.builders.generator;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;

public class PropertiesResolver {
    private final Class<?> entityClass;
    private final TypePrinter printer;

    public PropertiesResolver(Class<?> entityClass) {
        this.entityClass = entityClass;
        this.printer = new TypePrinter(entityClass);
    }

    public String getEntityTypeParameters() {
        return printer.printEntityTypeParameters();
    }

    public String getEntityTypeArguments() {
        return printer.printEntityTypeArguments();
    }

    public Collection<Property> resolveProperties() {
        Map<String, Property> properties = new HashMap<>();
        addProperties(entityClass, properties);
        return new TreeSet<>(properties.values());
    }

    /**
     * Properties generating the same builder method must only be added once. Java doesn't allow methods with the same
     * name and erasure, so that's what identifies a builder method. Setters win over fields, as they may contain logic,
     * and subclasses win over parent classes.
     */
    private void addProperty(Map<String, Property> properties, Property property) {
        String signature = property.nameStartingWithUppercase() + "(" + TypeToken.of(property.type).getRawType().getName() + ")";
        Property existing = properties.get(signature);
        if (existing == null || (property.isSetter && !existing.isSetter)) {
            properties.put(signature, property);
        }
    }

    private void addProperties(Class<?> clazz, Map<String, Property> properties) {
        if (clazz != null && clazz != Object.class) {
            addPublicFields(clazz, properties);
            addSetterMethods(clazz, properties);

            addProperties(clazz.getSuperclass(), properties);
        }
    }

    private void addPublicFields(Class<?> clazz, Map<String, Property> properties) {
        for (Field field : clazz.getDeclaredFields()) {
            if (isSuitableField(field)) {
                Property property = new Property();
                property.name = field.getName();
                setType(property, field.getGenericType());
                property.isSetter = false;
                addProperty(properties, property);
            }
        }
    }

    private void addSetterMethods(Class<?> clazz, Map<String, Property> properties) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (isSuitableMethod(method)) {
                Property property = new Property();
                property.name = method.getName().substring(3);
                setType(property, method.getGenericParameterTypes()[0]);
                property.isSetter = true;
                addProperty(properties, property);
            }
        }
    }

    private void setType(Property property, Type type) {
        property.type = printer.printableType(resolveType(type));
        property.printer = printer.forType(property.type);
    }

    /**
     * Resolves type variables of generic superclasses to the actual type arguments of the entity class.
     */
    private Type resolveType(Type type) {
        return TypeToken.of(entityClass).resolveType(type).getType();
    }

    private boolean isSuitableField(Field field) {
        return Modifier.isPublic(field.getModifiers()) &&
              !Modifier.isStatic(field.getModifiers()) &&
              !Modifier.isFinal(field.getModifiers());
    }

    private boolean isSuitableMethod(Method method) {
        return Modifier.isPublic(method.getModifiers()) &&
                !method.isBridge() &&
                method.getReturnType().equals(void.class) &&
                method.getParameterTypes().length == 1 &&
                method.getName().matches("^set[A-Z].*");
    }

    public static class Property implements Comparable<Property> {
        public String  name;
        public Type    type;
        public boolean isSetter;

        private TypePrinter printer;

        public String getTypeString() {
            return printer.printParameter(type);
        }

        public String getMethodTypeParameters() {
            return printer.printMethodTypeParameters();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Property property = (Property) o;

            return name.equals(property.name) && type.equals(property.type);
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + type.hashCode();
            return result;
        }

        @Override
        public int compareTo(Property o) {
            int result = name.compareTo(o.name);
            if (result == 0) {
                result = type.getTypeName().compareTo(o.type.getTypeName());
            }
            return result;
        }


        public String nameStartingWithUppercase() {
            return ("" + name.charAt(0)).toUpperCase() +  name.substring(1);
        }
    }
}
