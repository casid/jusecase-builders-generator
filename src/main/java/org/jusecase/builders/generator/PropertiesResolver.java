package org.jusecase.builders.generator;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;

public class PropertiesResolver {
    private final Class<?> entityClass;

    public PropertiesResolver(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    public Collection<Property> resolveProperties() {
        SortedSet<Property> properties = new TreeSet<>();
        addProperties(entityClass, properties);
        return properties;
    }

    private void addProperties(Class<?> clazz, Collection<Property> properties) {
        if (clazz != null && clazz != Object.class) {
            addPublicFields(clazz, properties);
            addSetterMethods(clazz, properties);

            addProperties(clazz.getSuperclass(), properties);
        }
    }

    private void addPublicFields(Class<?> clazz, Collection<Property> properties) {
        for (Field field : clazz.getDeclaredFields()) {
            if (isSuitableField(field)) {
                Property property = new Property();
                property.name = field.getName();
                property.type = field.getType();
                property.isSetter = false;
                properties.add(property);
            }
        }
    }

    private void addSetterMethods(Class<?> clazz, Collection<Property> properties) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (isSuitableMethod(method)) {
                Property property = new Property();
                property.name = method.getName().substring(3);
                property.type = method.getParameterTypes()[0];
                property.isSetter = true;
                properties.add(property);
            }
        }
    }

    private boolean isSuitableField(Field field) {
        return Modifier.isPublic(field.getModifiers()) &&
              !Modifier.isStatic(field.getModifiers()) &&
              !Modifier.isFinal(field.getModifiers());
    }

    private boolean isSuitableMethod(Method method) {
        return Modifier.isPublic(method.getModifiers()) &&
                method.getReturnType().equals(void.class) &&
                method.getParameterTypes().length == 1 &&
                method.getName().matches("^set[A-Z].*");
    }

    public static class Property implements Comparable<Property> {
        public String  name;
        public Type    type;
        public boolean isSetter;

        public String getTypeString() {
            String typeName;

            if (isArray(type)) {
              typeName = type.getTypeName();
            } else if ( isJavaLangType(type) ) {
                typeName = ((Class<?>) type).getSimpleName();
            } else {
                typeName = type.getTypeName();
            }

            return correctTypeName(typeName);
        }

        private static boolean isJavaLangType(Type type) {
            if (type instanceof Class<?>) {
                Class<?> clazz = (Class<?>)type;
                 return clazz.getPackage() == null || "java.lang".equals(clazz.getPackage().getName());
            }
            return false;
        }

        private static boolean isArray( Type type ) {
            if (type instanceof Class<?>) {
                Class<?> clazz = (Class<?>)type;
                return clazz.isArray();
            }
            return false;
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

        private String correctTypeName(String typeName) {
            typeName = correctNestedTypeName(typeName);
            typeName = correctArrayTypeName(typeName);
            return typeName;
        }

        private String correctArrayTypeName(String typeName) {
            if (typeName.endsWith("[]")) {
                typeName = typeName.substring(0, typeName.length() - 2) + " ...";
            }
            return typeName;
        }

        private String correctNestedTypeName(String typeName) {
            return typeName.replace('$', '.');
        }

        public String nameStartingWithUppercase() {
            return ("" + name.charAt(0)).toUpperCase() +  name.substring(1);
        }
    }
}
